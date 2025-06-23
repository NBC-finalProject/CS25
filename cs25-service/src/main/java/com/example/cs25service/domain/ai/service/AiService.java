package com.example.cs25service.domain.ai.service;


import com.example.cs25entity.domain.quiz.repository.QuizRepository;
import com.example.cs25entity.domain.subscription.repository.SubscriptionRepository;
import com.example.cs25entity.domain.user.entity.User;
import com.example.cs25entity.domain.user.exception.UserException;
import com.example.cs25entity.domain.user.exception.UserExceptionCode;
import com.example.cs25entity.domain.user.repository.UserRepository;
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
    private final UserQuizAnswerRepository userQuizAnswerRepository;
    private final RagService ragService;
    private final AiPromptProvider promptProvider;
    private final UserRepository userRepository;

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

        User user = userRepository.findById(answer.getUser().getId()).orElseThrow(
                () -> new UserException(UserExceptionCode.NOT_FOUND_USER)
        );

        // 점수 부여
        double score;
        if(isCorrect){
            score = user.getScore() + (quiz.getType().getScore() * quiz.getLevel().getExp());
        }else{
            score = user.getScore() + 1;
        }

        user.updateScore(score);
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
