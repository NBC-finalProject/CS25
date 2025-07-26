package com.example.cs25service.domain.ai.service;

import com.example.cs25entity.domain.quiz.repository.QuizRepository;
import com.example.cs25entity.domain.subscription.repository.SubscriptionRepository;
import com.example.cs25entity.domain.user.entity.User;
import com.example.cs25entity.domain.user.repository.UserRepository;
import com.example.cs25entity.domain.userQuizAnswer.repository.UserQuizAnswerRepository;
import com.example.cs25service.domain.ai.client.AiChatClient;
import com.example.cs25service.domain.ai.dto.response.AiFeedbackResponse;
import com.example.cs25service.domain.ai.exception.AiException;
import com.example.cs25service.domain.ai.exception.AiExceptionCode;
import com.example.cs25service.domain.ai.prompt.AiPromptProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
public class AiService {

    private final ChatClient chatClient;

    @Qualifier("fallbackAiChatClient")
    private final AiChatClient aiChatClient;

    private final AiFeedbackQueueService feedbackQueueService;
    private final QuizRepository quizRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserQuizAnswerRepository userQuizAnswerRepository;
    private final RagService ragService;
    private final AiPromptProvider promptProvider;
    private final UserRepository userRepository;

    public AiFeedbackResponse getFeedback(Long answerId) {
        var answer = userQuizAnswerRepository.findWithQuizAndUserByIdOrElseThrow(answerId);

        var quiz = answer.getQuiz();
        var docs = ragService.searchRelevant(quiz.getQuestion(), 3, 0.3);

        String userPrompt = promptProvider.getFeedbackUser(quiz, answer, docs);
        String systemPrompt = promptProvider.getFeedbackSystem();

        String feedback = aiChatClient.call(systemPrompt, userPrompt);

        //boolean isCorrect = feedback.startsWith("정답");
        boolean isCorrect = isCorrect(feedback);

        User user = answer.getUser();
        if (user != null) {
            double score =
                isCorrect ? user.getScore() + (quiz.getType().getScore() * quiz.getLevel().getExp())
                    : user.getScore() + 1;
            user.updateScore(score);
        }

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

    public SseEmitter streamFeedback(Long answerId, String mode) {
        SseEmitter emitter = new SseEmitter(60_000L);
        emitter.onTimeout(emitter::complete);
        emitter.onError(emitter::completeWithError);

        feedbackQueueService.enqueue(answerId, emitter);
        return emitter;
    }

    public boolean isCorrect(String feedback){
        if (feedback == null || feedback.isEmpty()) return false; //false 보다는 예외를 던지는게 더 나을 것 같음

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
}
