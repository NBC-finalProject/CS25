package com.example.cs25service.domain.ai.service;

import com.example.cs25entity.domain.quiz.repository.QuizRepository;
import com.example.cs25entity.domain.subscription.repository.SubscriptionRepository;
import com.example.cs25entity.domain.user.entity.User;
import com.example.cs25entity.domain.user.exception.UserException;
import com.example.cs25entity.domain.user.exception.UserExceptionCode;
import com.example.cs25entity.domain.user.repository.UserRepository;
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
    public void stream(Long answerId, SseEmitter emitter) {
        try {
            send(emitter, "🔍 유저 답변 조회 중...");
            var answer = userQuizAnswerRepository.findById(answerId)
                .orElseThrow(() -> new AiException(AiExceptionCode.NOT_FOUND_ANSWER));

            send(emitter, "📚 관련 문서 검색 중...");
            var quiz = answer.getQuiz();
            var docs = ragService.searchRelevant(quiz.getQuestion(), 3, 0.3);

            send(emitter, "🧠 프롬프트 생성 중...");
            String userPrompt = promptProvider.getFeedbackUser(quiz, answer, docs);
            String systemPrompt = promptProvider.getFeedbackSystem();

            send(emitter, "🤖 AI 응답 대기 중...");
            String feedback = aiChatClient.call(systemPrompt, userPrompt);
            String[] lines = feedback.split("(?<=[.!?]|다\\.|습니다\\.|입니다\\.)\\s*");

            for (String line : lines) {
                send(emitter, "🤖 " + line.trim());
            }

            boolean isCorrect = feedback.startsWith("정답");

            User user = userRepository.findById(answer.getUser().getId())
                .orElseThrow(() -> new UserException(UserExceptionCode.NOT_FOUND_USER));

            double score = isCorrect
                ? user.getScore() + (quiz.getType().getScore() * quiz.getLevel().getExp())
                : user.getScore() + 1;

            user.updateScore(score);
            answer.updateIsCorrect(isCorrect);
            answer.updateAiFeedback(feedback);
            userQuizAnswerRepository.save(answer);

            emitter.send(SseEmitter.event().name("complete").data("✅ 피드백 완료"));
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
