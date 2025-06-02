package com.example.cs25.domain.ai.service;

import com.example.cs25.domain.ai.dto.response.AiFeedbackResponse;
import com.example.cs25.domain.ai.exception.AiException;
import com.example.cs25.domain.ai.exception.AiExceptionCode;
import com.example.cs25.domain.quiz.repository.QuizRepository;
import com.example.cs25.domain.subscription.repository.SubscriptionRepository;
import com.example.cs25.domain.userQuizAnswer.repository.UserQuizAnswerRepository;
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

    public AiFeedbackResponse getFeedback(Long quizId, Long subscriptionId) {

        var quiz = quizRepository.findById(quizId)
            .orElseThrow(() -> new AiException(AiExceptionCode.NOT_FOUND_QUIZ));

        var answer = userQuizAnswerRepository.findFirstByQuizIdAndSubscriptionIdOrderByCreatedAtDesc(
                quizId, subscriptionId)
            .orElseThrow(() -> new AiException(AiExceptionCode.NOT_FOUND_ANSWER));

        String prompt = "문제: " + quiz.getQuestion() + "\n" +
            "사용자 답변: " + answer.getUserAnswer() + "\n" +
            "너는 CS 문제를 채점하는 AI 채점관이야. 아래 조건에 맞게 답변해.\n" +
            "1. 답변은 반드시 '정답' 또는 '오답'이라는 단어로 시작해야 해. 다른 단어로 시작하지 말 것.\n" +
            "2. '정답' 또는 '오답' 다음에는 채점 이유를 명확하게 작성해. (예: 문제 요구사항과 얼마나 일치하는지, 핵심 개념이 잘 설명되었는지 등)\n" +
            "3. 그 다음에는 사용자 답변에 대한 구체적인 피드백을 작성해. (어떤 부분이 잘 되었고, 어떤 부분을 개선해야 하는지)\n" +
            "4. 다른 표현(예: '맞습니다', '틀렸습니다')은 사용하지 말고, 무조건 '정답' 또는 '오답'으로 시작해.\n" +
            "5. 예시:\n" +
            "- 정답: 답변이 문제의 요구사항을 정확히 충족하며, 네트워크 계층의 개념을 올바르게 설명했다. 피드백: 전체적인 설명이 명확하며, 추가적으로 HTTP 상태 코드의 예시를 들어주면 더 좋겠다.\n"
            +
            "- 오답: 사용자가 작성한 답변이 문제의 요구사항을 충족하지 못했고, TCP와 UDP의 차이점을 명확하게 설명하지 못했다. 피드백: TCP의 연결 방식과 UDP의 비연결 방식 차이에 대한 구체적인 설명이 필요하다.\n"
            +
            "위 조건을 반드시 지켜서 평가해줘.";

        String feedback;
        try {
            feedback = chatClient.prompt()
                .system("너는 CS 지식을 평가하는 채점관이야. 문제와 답변을 보고 '정답' 또는 '오답'으로 시작하는 문장으로 답변해. " +
                    "다른 단어나 표현은 사용하지 말고, 반드시 '정답' 또는 '오답'으로 시작해. " +
                    "그리고 사용자 답변에 대한 피드백도 반드시 작성해.")
                .user(prompt)
                .call()
                .content();
        } catch (Exception e) {
            throw new AiException(AiExceptionCode.INTERNAL_SERVER_ERROR);
        }

        boolean isCorrect = feedback.trim().startsWith("정답");

        answer.updateIsCorrect(isCorrect);
        answer.updateAiFeedback(feedback);
        userQuizAnswerRepository.save(answer);

        return new AiFeedbackResponse(
            quiz.getId(),
            isCorrect,
            feedback,
            answer.getId()
        );
    }
}
