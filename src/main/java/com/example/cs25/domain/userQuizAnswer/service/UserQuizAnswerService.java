package com.example.cs25.domain.userQuizAnswer.service;

import com.example.cs25.domain.quiz.entity.Quiz;
import com.example.cs25.domain.quiz.exception.QuizException;
import com.example.cs25.domain.quiz.exception.QuizExceptionCode;
import com.example.cs25.domain.quiz.repository.QuizRepository;
import com.example.cs25.domain.subscription.entity.Subscription;
import com.example.cs25.domain.subscription.exception.SubscriptionException;
import com.example.cs25.domain.subscription.exception.SubscriptionExceptionCode;
import com.example.cs25.domain.subscription.repository.SubscriptionRepository;
import com.example.cs25.domain.userQuizAnswer.dto.SelectionRateResponseDto;
import com.example.cs25.domain.userQuizAnswer.dto.UserAnswerDto;
import com.example.cs25.domain.userQuizAnswer.entity.UserQuizAnswer;
import com.example.cs25.domain.userQuizAnswer.repository.UserQuizAnswerCustomRepository;
import com.example.cs25.domain.userQuizAnswer.repository.UserQuizAnswerRepository;
import com.example.cs25.domain.userQuizAnswer.dto.UserQuizAnswerRequestDto;
import com.example.cs25.domain.users.entity.User;
import com.example.cs25.domain.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    public SelectionRateResponseDto getSelectionRateByOption(Long quizId) {
        List<UserAnswerDto> answers = userQuizAnswerRepository.findUserAnswerByQuizId(quizId);

        //보기별 선택 수 집계
        Map<String, Long> counts = answers.stream()
                .map(UserAnswerDto::getUserAnswer)
                .filter(Objects::nonNull)
                .map(String::trim)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        // 총 응답 수 계산
        long total = counts.values().stream().mapToLong(Long::longValue).sum();

        // 선택률 계산
        Map<String, Double> rates = counts.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> (double) e.getValue() / total
                ));

        return new SelectionRateResponseDto(rates, total);
    }
}
