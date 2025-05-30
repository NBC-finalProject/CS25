package com.example.cs25.domain.ai.service;

import com.example.cs25.domain.ai.dto.response.AiFeedbackResponse;
import com.example.cs25.domain.ai.exception.AiException;
import com.example.cs25.domain.ai.exception.AiExceptionCode;
import com.example.cs25.domain.quiz.repository.QuizRepository;
import com.example.cs25.domain.userQuizAnswer.repository.UserQuizAnswerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiService {

    private final ChatClient chatClient;
    private final QuizRepository quizRepository;
    private final UserQuizAnswerRepository userQuizAnswerRepository;

    public AiFeedbackResponse getFeedback(Long quizId) {
        var quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new AiException(AiExceptionCode.NOT_FOUND_QUIZ));

        var answer = userQuizAnswerRepository.findFirstByQuizIdOrderByCreatedAtDesc(quizId)
                .orElseThrow(() -> new AiException(AiExceptionCode.NOT_FOUND_ANSWER));

        String prompt = "문제: " + quiz.getQuestion() + "\n" +
                "사용자 답변: " + answer.getUserAnswer() + "\n" +
                "위의 답변을 평가해. 정답 여부를 알려주고, 이유를 한 문장으로 작성해.";

        String feedback;
        try {
            feedback = chatClient.prompt()
                    .system("너는 CS 지식을 평가하는 채점관이야. 사용자가 작성한 답변을 평가해서 '정답' 또는 '오답'이라고 알려주고," +
                            "명확한 피드백을 작성해줘. ")
                    .user(prompt)
                    .call()
                    .content();
        } catch (Exception e) {
            throw new AiException(AiExceptionCode.INTERNAL_SERVER_ERROR);
        }

        return new AiFeedbackResponse(
                quiz.getId(),
                answer.getIsCorrect(),
                feedback,
                answer.getId()
        );
    }
}
