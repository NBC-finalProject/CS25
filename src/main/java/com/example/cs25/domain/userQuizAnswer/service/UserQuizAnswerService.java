package com.example.cs25.domain.userQuizAnswer.service;

import com.example.cs25.domain.quiz.entity.Quiz;
import com.example.cs25.domain.quiz.exception.QuizException;
import com.example.cs25.domain.quiz.exception.QuizExceptionCode;
import com.example.cs25.domain.quiz.repository.QuizRepository;
import com.example.cs25.domain.subscription.entity.Subscription;
import com.example.cs25.domain.subscription.exception.SubscriptionException;
import com.example.cs25.domain.subscription.exception.SubscriptionExceptionCode;
import com.example.cs25.domain.subscription.repository.SubscriptionRepository;
import com.example.cs25.domain.userQuizAnswer.entity.UserQuizAnswer;
import com.example.cs25.domain.userQuizAnswer.repository.UserQuizAnswerRepository;
import com.example.cs25.domain.userQuizAnswer.requestDto.UserQuizAnswerRequestDto;
import com.example.cs25.domain.users.entity.User;
import com.example.cs25.domain.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserQuizAnswerService {

    private final UserQuizAnswerRepository userQuizAnswerRepository;
    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;

    public void answerSubmit(Long quizId, UserQuizAnswerRequestDto requestDto) {

        // 구독 정보 조회
        Subscription subscription = subscriptionRepository.findById(requestDto.getSubscriptionId())
                .orElseThrow(() -> new SubscriptionException(SubscriptionExceptionCode.NOT_FOUND_SUBSCRIPTION_ERROR));

        // 유저 정보 조회
        User user = userRepository.findBySubscription(subscription);

        // 퀴즈 조회
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new QuizException(QuizExceptionCode.NOT_FOUND_ERROR));

        // 정답 체크
        boolean isCorrect = requestDto.getAnswer().equals(quiz.getAnswer().substring(0,1));

        userQuizAnswerRepository.save(
                UserQuizAnswer.builder()
                .userAnswer(requestDto.getAnswer())
                .isCorrect(isCorrect)
                .user(user)
                .quiz(quiz)
                .subscription(subscription)
                .build()
        );
    }
}
