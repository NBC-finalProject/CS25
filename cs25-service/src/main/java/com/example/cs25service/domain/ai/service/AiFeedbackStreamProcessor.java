package com.example.cs25service.domain.ai.service;

import com.example.cs25entity.domain.quiz.entity.Quiz;
import com.example.cs25entity.domain.user.entity.User;
import com.example.cs25entity.domain.user.repository.UserRepository;
import com.example.cs25entity.domain.userQuizAnswer.entity.UserQuizAnswer;
import com.example.cs25entity.domain.userQuizAnswer.repository.UserQuizAnswerRepository;
import com.example.cs25service.domain.ai.client.AiChatClient;
import com.example.cs25service.domain.ai.exception.AiException;
import com.example.cs25service.domain.ai.exception.AiExceptionCode;
import com.example.cs25service.domain.ai.prompt.AiPromptProvider;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
@RequiredArgsConstructor
public class AiFeedbackStreamProcessor {

    private final UserQuizAnswerRepository userQuizAnswerRepository;
    private final AiPromptProvider promptProvider;
    private final RagService ragService;
    private final UserRepository userRepository;
    private final AiChatClient aiChatClient;

    @Transactional
    public void streamWord(Long answerId, SseEmitter emitter) {
        try {
            var answer = prepare(answerId, emitter);
            if (answer == null) {
                return;
            }

            var quiz = answer.getQuiz();
            var docs = ragService.searchRelevant(quiz.getQuestion(), 3, 0.3);
            String userPrompt = promptProvider.getFeedbackUser(quiz, answer, docs);
            String systemPrompt = promptProvider.getFeedbackSystem();

            send(emitter, "AI 응답 대기 중...");

            StringBuilder wordBuffer = new StringBuilder();

            aiChatClient.stream(systemPrompt, userPrompt)
                .doOnNext(token -> {
                    wordBuffer.append(token);
                    if (token.equals(" ") || token.matches("[.,!?]")) {
                        send(emitter, wordBuffer.toString());
                        wordBuffer.setLength(0);
                    }
                })
                .doOnComplete(() -> finalizeFeedback(answer, quiz, wordBuffer, emitter))
                .doOnError(emitter::completeWithError)
                .subscribe();

        } catch (Exception e) {
            emitter.completeWithError(e);
        }
    }

    @Transactional
    public void streamSentence(Long answerId, SseEmitter emitter) {
        try {
            var answer = prepare(answerId, emitter);
            if (answer == null) {
                return;
            }

            var quiz = answer.getQuiz();
            var docs = ragService.searchRelevant(quiz.getQuestion(), 3, 0.3);
            String userPrompt = promptProvider.getFeedbackUser(quiz, answer, docs);
            String systemPrompt = promptProvider.getFeedbackSystem();

            send(emitter, "AI 응답 대기 중...");

            StringBuilder sentenceBuffer = new StringBuilder();

            aiChatClient.stream(systemPrompt, userPrompt)
                .doOnNext(token -> {
                    sentenceBuffer.append(token);
                    if (token.matches("[.!?]")) {
                        send(emitter, sentenceBuffer.toString());
                        sentenceBuffer.setLength(0);
                    }
                })
                .doOnComplete(() -> finalizeFeedback(answer, quiz, sentenceBuffer, emitter))
                .doOnError(emitter::completeWithError)
                .subscribe();

        } catch (Exception e) {
            emitter.completeWithError(e);
        }
    }

    private UserQuizAnswer prepare(Long answerId, SseEmitter emitter) throws IOException {
        emitter.onTimeout(emitter::complete);
        emitter.onError(emitter::completeWithError);

        var answer = userQuizAnswerRepository.findById(answerId)
            .orElseThrow(() -> new AiException(AiExceptionCode.NOT_FOUND_ANSWER));

        if (answer.getAiFeedback() != null) {
            emitter.send(SseEmitter.event().data("이미 처리된 요청입니다."));
            emitter.complete();
            return null;
        }
        return answer;
    }

    private void finalizeFeedback(UserQuizAnswer answer, Quiz quiz, StringBuilder buffer,
        SseEmitter emitter) {
        try {
            if (buffer.length() > 0) {
                send(emitter, buffer.toString());
            }

            String feedback =
                answer.getAiFeedback() != null ? answer.getAiFeedback() : buffer.toString();
            boolean isCorrect = feedback.startsWith("정답");

            User user = answer.getUser();
            if (user != null) {
                double score = isCorrect
                    ? user.getScore() + (quiz.getType().getScore() * quiz.getLevel().getExp())
                    : user.getScore() + 1;
                user.updateScore(score);
            }

            answer.updateIsCorrect(isCorrect);
            answer.updateAiFeedback(feedback);
            userQuizAnswerRepository.save(answer);

            emitter.complete();
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
    }

    private void send(SseEmitter emitter, String data) {
        try {
            emitter.send(SseEmitter.event().data(data));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
    }
}
