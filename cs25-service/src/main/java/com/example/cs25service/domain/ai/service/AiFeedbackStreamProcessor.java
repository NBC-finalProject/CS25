package com.example.cs25service.domain.ai.service;

import com.example.cs25entity.domain.user.entity.User;
import com.example.cs25entity.domain.user.repository.UserRepository;
import com.example.cs25entity.domain.userQuizAnswer.repository.UserQuizAnswerRepository;
import com.example.cs25service.domain.ai.client.AiChatClient;
import com.example.cs25service.domain.ai.exception.AiException;
import com.example.cs25service.domain.ai.exception.AiExceptionCode;
import com.example.cs25service.domain.ai.prompt.AiPromptProvider;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Transactional
    public void stream(Long answerId, SseEmitter emitter) {
        try {
            var answer = userQuizAnswerRepository.findById(answerId)
                .orElseThrow(() -> new AiException(AiExceptionCode.NOT_FOUND_ANSWER));

            if (answer.getAiFeedback() != null) {
                emitter.send(SseEmitter.event().data("이미 처리된 요청입니다."));
                emitter.complete();
                return;
            }

            var quiz = answer.getQuiz();
            var docs = ragService.searchRelevant(quiz.getQuestion(), 3, 0.3);
            String userPrompt = promptProvider.getFeedbackUser(quiz, answer, docs);
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
                        send(emitter, "[DONE]");
                        Thread.sleep(100);

                        String feedback = fullFeedbackBuffer.toString();
                        boolean isCorrect = feedback.startsWith("정답");

                        transactionTemplate.executeWithoutResult(status -> {
                            if (user != null && userScore != null) {
                                double score = isCorrect
                                    ? userScore + (quiz.getType().getScore() * quiz.getLevel().getExp())
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
