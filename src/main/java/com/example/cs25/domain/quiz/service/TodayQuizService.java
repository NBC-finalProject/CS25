package com.example.cs25.domain.quiz.service;

import com.example.cs25.domain.quiz.entity.Quiz;
import com.example.cs25.domain.quiz.exception.QuizException;
import com.example.cs25.domain.quiz.exception.QuizExceptionCode;
import com.example.cs25.domain.quiz.repository.QuizRepository;
import com.example.cs25.domain.subscription.entity.Subscription;
import com.example.cs25.domain.subscription.repository.SubscriptionRepository;
import com.example.cs25.domain.userQuizAnswer.entity.UserQuizAnswer;
import com.example.cs25.domain.userQuizAnswer.repository.UserQuizAnswerRepository;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * SubscriptionRepository, UserQuizAnswerRepository 참조를 하기때문에 따로 뗏음
 */
@Service
@RequiredArgsConstructor
public class TodayQuizService {

    private final QuizRepository quizRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserQuizAnswerRepository userQuizAnswerRepository;

    @Transactional
    public Quiz getTodayQuiz(Long subscriptionId) {
        //해당 구독자의 문제 구독 카테고리 확인
        Subscription subscription = subscriptionRepository.findByIdOrElseThrow(subscriptionId);

        //id 순으로 정렬
        List<Quiz> quizList = quizRepository.findAllByCategoryId(
                subscription.getCategory().getId())
            .stream()
            .sorted(Comparator.comparing(Quiz::getId))
            .toList();

        if (quizList.isEmpty()) {
            throw new QuizException(QuizExceptionCode.NO_QUIZ_EXISTS_ERROR);
        }

        // 구독 시작일 기준 날짜 차이 계산
        LocalDate createdDate = subscription.getCreatedAt().toLocalDate();
        LocalDate today = LocalDate.now();
        long daysSinceCreated = ChronoUnit.DAYS.between(createdDate, today);

        // 슬라이딩 인덱스로 문제 선택
        int offset = Math.toIntExact((subscriptionId + daysSinceCreated) % quizList.size());
        return quizList.get(offset);
    }

    @Transactional
    public Quiz getTodayQuizNew(Long subscriptionId) {
        //1. 해당 구독자의 문제 구독 카테고리 확인
        Subscription subscription = subscriptionRepository.findByIdOrElseThrow(subscriptionId);
        Long categoryId = subscription.getCategory().getId();
        Long userId = subscription.getId();

        // 2. 유저의 정답률 계산
        List<UserQuizAnswer> answers = userQuizAnswerRepository.findByUserIdAndCategoryId(userId,
            categoryId);
        double userAccuracy = calculateAccuracy(answers); // 정답 수 / 전체 수

        // 4. 유저가 푼 문제 ID 목록
        Set<Long> solvedQuizIds = answers.stream()
            .map(answer -> answer.getQuiz().getId())
            .collect(Collectors.toSet());

        // 5. 가장 비슷한 정답률을 가진 안푼 문제 찾기
        return quizAccuracyMap.entrySet().stream()
            .filter(entry -> !solvedQuizIds.contains(entry.getKey()))
            .min(Comparator.comparingDouble(entry -> Math.abs(entry.getValue() - userAccuracy)))
            .map(entry -> quizRepository.findById(entry.getKey()).orElse(null))
            .orElse(null); // 없으면 null 또는 랜덤
    }

    private double calculateAccuracy(List<UserQuizAnswer> answers) {
        int totalCorrect = 0;
        for (UserQuizAnswer answer : answers) {
            if (answer.getIsCorrect()) {
                totalCorrect++;
            }
        }
        return (double) totalCorrect / answers.size();
    }

}
