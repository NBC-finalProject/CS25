package com.example.cs25service.domain.ai.service;

import com.example.cs25entity.domain.user.entity.User;
import com.example.cs25entity.domain.user.repository.UserRepository;
import com.example.cs25entity.domain.userQuizAnswer.repository.UserQuizAnswerRepository;
import com.example.cs25service.domain.ai.client.AiChatClient;
//import com.example.cs25service.domain.ai.exception.AiException;
//import com.example.cs25service.domain.ai.exception.AiExceptionCode;
import com.example.cs25service.domain.ai.prompt.AiPromptProvider;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiFeedbackStreamProcessor {

    private final UserQuizAnswerRepository userQuizAnswerRepository;
    private final AiPromptProvider promptProvider;
    private final RagService ragService;
    private final UserRepository userRepository;
    private final AiChatClient aiChatClient;
    private final TransactionTemplate transactionTemplate;
    private final BraveSearchRagService braveSearchRagService;
    private final BraveSearchMcpService braveSearchMcpService;

    @Transactional
    public void stream(Long answerId, SseEmitter emitter) {
        try {
            var answer = userQuizAnswerRepository.findByIdOrElseThrow(answerId);

            if (answer.getAiFeedback() != null) {
                emitter.send(SseEmitter.event().data("이미 처리된 요청입니다."));
                emitter.complete();
                return;
            }

            var quiz = answer.getQuiz();
            var vectorDocs = ragService.searchRelevant(quiz.getQuestion(), 2, 0.5);
            Optional<JsonNode> braveResults = Optional.empty();
            List<Document> webDocs = new ArrayList<>();
            try {
                JsonNode searchResult = braveSearchMcpService.search(quiz.getQuestion(), 2, 0);
                braveResults = Optional.ofNullable(searchResult);
                webDocs = braveSearchRagService.toDocuments(braveResults);
                log.debug(" Brave 검색 결과 문서 {}개를 성공적으로 가져왔습니다.", webDocs.size());
            } catch (Exception e) {
                log.warn("⚠ Brave 검색 실패 - 질문: [{}], 벡터 검색만 사용합니다.", quiz.getQuestion(), e);
            }

            List<Document> docs = new ArrayList<>();
            docs.addAll(vectorDocs);
            docs.addAll(webDocs);

            String userPrompt = promptProvider.getFeedbackUser(quiz, answer, docs, braveResults);
            String systemPrompt = promptProvider.getFeedbackSystem();

            User user = answer.getUser();
            Double userScore = user != null ? user.getScore() : null;

            send(emitter, "AI 응답 대기 중...");

            StringBuilder sentenceBuffer = new StringBuilder();
            StringBuilder fullFeedbackBuffer = new StringBuilder();

            aiChatClient.stream(systemPrompt, userPrompt)
                .doOnNext(token -> {
                    sentenceBuffer.append(token);
                    fullFeedbackBuffer.append(token);

                    if (token.matches("[.!?]")) {
                        send(emitter, sentenceBuffer.toString());
                        sentenceBuffer.setLength(0);
                    }
                })
                .doOnComplete(() -> {
                    try {
                        if (sentenceBuffer.length() > 0) {
                            send(emitter, sentenceBuffer.toString());
                        }
                        send(emitter, "[종료]");

                        String feedback = fullFeedbackBuffer.toString();
                        //서비스 흐름 상 예외를 던지는 유효성 검증이 옳은지 논의 필요
//                        if (feedback == null || feedback.isEmpty()) {
//                            throw new AiException(AiExceptionCode.INTERNAL_SERVER_ERROR);
//                        }

                        boolean isCorrect = isCorrect(feedback);

                        transactionTemplate.executeWithoutResult(status -> {
                            if (user != null && userScore != null) {
                                double score = isCorrect
                                    ? userScore + (quiz.getType().getScore() * quiz.getLevel()
                                    .getExp())
                                    : userScore + 1;
                                user.updateScore(score);
                                userRepository.save(user);
                            }
                            answer.updateIsCorrect(isCorrect);
                            answer.updateAiFeedback(feedback);
                            userQuizAnswerRepository.save(answer);
                        });

                        emitter.complete();
                    } catch (Exception e) {
                        emitter.completeWithError(e);
                    }
                })
                .doOnError(emitter::completeWithError)
                .subscribe();

        } catch (Exception e) {
            emitter.completeWithError(e);
        }
    }

    public boolean isCorrect(String feedback){
        String prefix = feedback.length() > 6
            ? feedback.substring(0, 6)
            : feedback;

        int indexCorrect = prefix.indexOf("정답");
        int indexWrong = prefix.indexOf("오답");

        if (indexCorrect != -1 && (indexWrong == -1 || indexCorrect < indexWrong)) {
            return true;
        }

        return false;
    }

    private void send(SseEmitter emitter, String data) {
        try {
            emitter.send(SseEmitter.event().data(data));
        } catch (IOException e) {
            log.error("SSE send error: {}", e.getMessage(), e);
            emitter.completeWithError(e);
            throw new RuntimeException(e);
        }
    }
}
