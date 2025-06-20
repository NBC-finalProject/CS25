package com.example.cs25service.domain.userQuizAnswer.service;

import com.example.cs25entity.domain.quiz.entity.Quiz;
import com.example.cs25entity.domain.quiz.exception.QuizException;
import com.example.cs25entity.domain.quiz.exception.QuizExceptionCode;
import com.example.cs25entity.domain.quiz.repository.QuizRepository;
import com.example.cs25entity.domain.subscription.entity.Subscription;
import com.example.cs25entity.domain.subscription.exception.SubscriptionException;
import com.example.cs25entity.domain.subscription.exception.SubscriptionExceptionCode;
import com.example.cs25entity.domain.subscription.repository.SubscriptionRepository;
import com.example.cs25entity.domain.user.entity.User;
import com.example.cs25entity.domain.user.repository.UserRepository;
import com.example.cs25entity.domain.userQuizAnswer.dto.UserAnswerDto;
import com.example.cs25entity.domain.userQuizAnswer.entity.UserQuizAnswer;
import com.example.cs25entity.domain.userQuizAnswer.exception.UserQuizAnswerException;
import com.example.cs25entity.domain.userQuizAnswer.exception.UserQuizAnswerExceptionCode;
import com.example.cs25entity.domain.userQuizAnswer.repository.UserQuizAnswerRepository;
import com.example.cs25service.domain.userQuizAnswer.dto.SelectionRateResponseDto;
import com.example.cs25service.domain.userQuizAnswer.dto.UserQuizAnswerRequestDto;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserQuizAnswerService {

    private final UserQuizAnswerRepository userQuizAnswerRepository;
    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;

    public Long answerSubmit(Long quizId, UserQuizAnswerRequestDto requestDto) {
        // 중복 답변 제출 막음
        boolean isDuplicate = userQuizAnswerRepository.existsByQuizIdAndSubscriptionId(quizId, requestDto.getSubscriptionId());
        if (isDuplicate) {
            throw new UserQuizAnswerException(UserQuizAnswerExceptionCode.DUPLICATED_ANSWER);
        }

        // 구독 정보 조회
        Subscription subscription = subscriptionRepository.findById(requestDto.getSubscriptionId())
            .orElseThrow(() -> new SubscriptionException(
                SubscriptionExceptionCode.NOT_FOUND_SUBSCRIPTION_ERROR));

        // 유저 정보 조회
        User user = userRepository.findBySubscription(subscription);

        // 퀴즈 조회
        Quiz quiz = quizRepository.findById(quizId)
            .orElseThrow(() -> new QuizException(QuizExceptionCode.NOT_FOUND_ERROR));

        // 정답 체크
        boolean isCorrect = requestDto.getAnswer().equals(quiz.getAnswer().substring(0, 1));

        UserQuizAnswer answer = userQuizAnswerRepository.save(
            UserQuizAnswer.builder()
                .userAnswer(requestDto.getAnswer())
                .isCorrect(isCorrect)
                .user(user)
                .quiz(quiz)
                .subscription(subscription)
                .build()
        );
        return answer.getId();
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
