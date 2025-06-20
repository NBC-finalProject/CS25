package com.example.cs25service.domain.ai.service;


import com.example.cs25entity.domain.quiz.repository.QuizRepository;
import com.example.cs25entity.domain.subscription.repository.SubscriptionRepository;
import com.example.cs25entity.domain.userQuizAnswer.repository.UserQuizAnswerRepository;
import com.example.cs25service.domain.ai.dto.response.AiFeedbackResponse;
import com.example.cs25service.domain.ai.exception.AiException;
import com.example.cs25service.domain.ai.exception.AiExceptionCode;
import com.example.cs25service.domain.ai.prompt.AiPromptProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiService {

    private final ChatClient chatClient;
    private final QuizRepository quizRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserQuizAnswerRepository userQuizAnswerRepository;
    private final RagService ragService;
    private final AiPromptProvider promptProvider;

    public AiFeedbackResponse getFeedback(Long answerId) {
        var answer = userQuizAnswerRepository.findById(answerId)
            .orElseThrow(() -> new AiException(AiExceptionCode.NOT_FOUND_ANSWER));

        var quiz = answer.getQuiz();
        var docs = ragService.searchRelevant(quiz.getQuestion(), 3, 0.1);

        String userPrompt = promptProvider.getFeedbackUser(quiz, answer, docs);
        String systemPrompt = promptProvider.getFeedbackSystem();

        String feedback;
        try {
            feedback = chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content()
                .trim();
        } catch (Exception e) {
            throw new AiException(AiExceptionCode.INTERNAL_SERVER_ERROR);
        }

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
}
