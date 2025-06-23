package com.example.cs25service.domain.ai.service;


import com.example.cs25entity.domain.quiz.repository.QuizRepository;
import com.example.cs25entity.domain.subscription.repository.SubscriptionRepository;
import com.example.cs25entity.domain.userQuizAnswer.repository.UserQuizAnswerRepository;
import com.example.cs25service.domain.ai.client.AiChatClient;
import com.example.cs25service.domain.ai.dto.response.AiFeedbackResponse;
import com.example.cs25service.domain.ai.exception.AiException;
import com.example.cs25service.domain.ai.exception.AiExceptionCode;
import com.example.cs25service.domain.ai.prompt.AiPromptProvider;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
public class AiService {

    @Qualifier("fallbackAiChatClient")
    private final AiChatClient aiChatClient;

    private final QuizRepository quizRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserQuizAnswerRepository userQuizAnswerRepository;
    private final RagService ragService;
    private final AiPromptProvider promptProvider;

    public AiFeedbackResponse getFeedback(Long answerId) {
        var answer = userQuizAnswerRepository.findById(answerId)
            .orElseThrow(() -> new AiException(AiExceptionCode.NOT_FOUND_ANSWER));

        var quiz = answer.getQuiz();
        var docs = ragService.searchRelevant(quiz.getQuestion(), 3, 0.3);

        String userPrompt = promptProvider.getFeedbackUser(quiz, answer, docs);
        String systemPrompt = promptProvider.getFeedbackSystem();

        String feedback = aiChatClient.call(systemPrompt, userPrompt);
        boolean isCorrect = feedback.startsWith("정답");

        answer.updateIsCorrect(isCorrect);
        answer.updateAiFeedback(feedback);
        userQuizAnswerRepository.save(answer);

        return AiFeedbackResponse.builder()
            .quizId(quiz.getId())
            .quizAnswerId(answer.getId())
            .isCorrect(isCorrect)
            .aiFeedback(feedback)
            .build();
    }

    @Async
    public SseEmitter streamFeedback(Long answerId) {
        SseEmitter emitter = new SseEmitter(60_000L); // 1분 제한

        emitter.onTimeout(() -> {
            emitter.complete();
        });

        emitter.onError((ex) -> {
            emitter.completeWithError(ex);
        });

        CompletableFuture.runAsync(() -> {
            try {
                sendSseEvent(emitter,"🔍 유저 답변 조회 중...");
                var answer = userQuizAnswerRepository.findById(answerId)
                    .orElseThrow(() -> new AiException(AiExceptionCode.NOT_FOUND_ANSWER));

                sendSseEvent(emitter,"📚 관련 문서 검색 중...");
                var quiz = answer.getQuiz();
                var docs = ragService.searchRelevant(quiz.getQuestion(), 3, 0.3);

                sendSseEvent(emitter,"🧠 프롬프트 생성 중...");
                String userPrompt = promptProvider.getFeedbackUser(quiz, answer, docs);
                String systemPrompt = promptProvider.getFeedbackSystem();

                // AI 응답 생성
                sendSseEvent(emitter,"🤖 AI 응답 대기 중...");
                String feedback = aiChatClient.call(systemPrompt, userPrompt);

                // 문장 단위 분할
                String[] lines = feedback.split("(?<=[.!?]|다\\.|습니다\\.|입니다\\.)\\s*");

                for (String line : lines) {
                    sendSseEvent(emitter,"🤖 " + line.trim());
                }

                // 정답 여부 판별 및 저장
                boolean isCorrect = feedback.startsWith("정답");
                answer.updateIsCorrect(isCorrect);
                answer.updateAiFeedback(feedback);
                userQuizAnswerRepository.save(answer);

                emitter.send(SseEmitter.event().name("complete").data("✅ 피드백 완료"));
                emitter.complete();

            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }
    private void sendSseEvent(SseEmitter emitter, String data) {
               try {
                       emitter.send(SseEmitter.event().data(data));
                   } catch (IOException e) {
                       emitter.completeWithError(e);
                   }
            }
}
