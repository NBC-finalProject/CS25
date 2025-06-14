package com.example.cs25.domain.quiz.service;

import com.example.cs25.domain.mail.service.MailService;
import com.example.cs25.domain.quiz.dto.QuizDto;
import com.example.cs25.domain.quiz.entity.Quiz;
import com.example.cs25.domain.quiz.entity.QuizAccuracy;
import com.example.cs25.domain.quiz.exception.QuizException;
import com.example.cs25.domain.quiz.exception.QuizExceptionCode;
import com.example.cs25.domain.quiz.repository.QuizAccuracyRedisRepository;
import com.example.cs25.domain.quiz.repository.QuizRepository;
import com.example.cs25.domain.subscription.entity.Subscription;
import com.example.cs25.domain.subscription.repository.SubscriptionRepository;
import com.example.cs25.domain.userQuizAnswer.entity.UserQuizAnswer;
import com.example.cs25.domain.userQuizAnswer.repository.UserQuizAnswerRepository;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * SubscriptionRepository, UserQuizAnswerRepository,QuizAccuracyRedisRepository 참조를 하기때문에 따로 뗏음
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TodayQuizService {

    private final QuizRepository quizRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserQuizAnswerRepository userQuizAnswerRepository;
    private final QuizAccuracyRedisRepository quizAccuracyRedisRepository;
    private final MailService mailService;

    @Transactional
    public QuizDto getTodayQuiz(Long subscriptionId) {
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
        Quiz selectedQuiz = quizList.get(offset);

        //return selectedQuiz;
        return QuizDto.builder()
            .id(selectedQuiz.getId())
            .quizCategory(selectedQuiz.getCategory().getCategoryType())
            .question(selectedQuiz.getQuestion())
            .choice(selectedQuiz.getChoice())
            .type(selectedQuiz.getType())
            .build();  //return -> QuizDto
    }

    @Transactional
    public Quiz getTodayQuizBySubscription(Subscription subscription) {
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
        int offset = Math.toIntExact((subscription.getId() + daysSinceCreated) % quizList.size());

        //return selectedQuiz;
        return quizList.get(offset);
    }

    @Transactional
    public QuizDto getTodayQuizNew(Long subscriptionId) {
        //1. 해당 구독자의 문제 구독 카테고리 확인
        Subscription subscription = subscriptionRepository.findByIdOrElseThrow(subscriptionId);
        Long categoryId = subscription.getCategory().getId();

        // 2. 유저의 정답률 계산
        List<UserQuizAnswer> answers = userQuizAnswerRepository.findByUserIdAndCategoryId(
            subscriptionId,
            categoryId);
        double userAccuracy = calculateAccuracy(answers); // 정답 수 / 전체 수

        log.info("✳ getTodayQuizNew  유저의 정답률 계산 : {}", userAccuracy);
        // 3. Redis에서 정답률 리스트 가져오기
        List<QuizAccuracy> accuracyList = quizAccuracyRedisRepository.findAllByCategoryId(
            categoryId);
        //  QuizAccuracy 리스트를 Map<quizId, accuracy>로 변환
        Map<Long, Double> quizAccuracyMap = accuracyList.stream()
            .collect(Collectors.toMap(QuizAccuracy::getQuizId, QuizAccuracy::getAccuracy));

        // 4. 유저가 푼 문제 ID 목록
        Set<Long> solvedQuizIds = answers.stream()
            .map(answer -> answer.getQuiz().getId())
            .collect(Collectors.toSet());

        // 5. 가장 비슷한 정답률을 가진 안푼 문제 찾기
        Quiz selectedQuiz = quizAccuracyMap.entrySet().stream()
            .filter(entry -> !solvedQuizIds.contains(entry.getKey()))
            .min(Comparator.comparingDouble(entry -> Math.abs(entry.getValue() - userAccuracy)))
            .flatMap(entry -> quizRepository.findById(entry.getKey()))
            .orElse(null); // 없으면 null 또는 랜덤

        if (selectedQuiz == null) {
            throw new QuizException(QuizExceptionCode.NO_QUIZ_EXISTS_ERROR);
        }
        //return selectedQuiz;   //return -> Quiz
        return QuizDto.builder()
            .id(selectedQuiz.getId())
            .quizCategory(selectedQuiz.getCategory().getCategoryType())
            .question(selectedQuiz.getQuestion())
            .choice(selectedQuiz.getChoice())
            .type(selectedQuiz.getType())
            .build(); //return -> QuizDto

    }

    private double calculateAccuracy(List<UserQuizAnswer> answers) {
        if (answers.isEmpty()) {
            return 0.0;
        }

        int totalCorrect = 0;
        for (UserQuizAnswer answer : answers) {
            if (answer.getIsCorrect()) {
                totalCorrect++;
            }
        }
        return ((double) totalCorrect / answers.size()) * 100.0;
    }

    public void calculateAndCacheAllQuizAccuracies() {
        List<Quiz> quizzes = quizRepository.findAll();

        List<QuizAccuracy> accuracyList = new ArrayList<>();
        for (Quiz quiz : quizzes) {

            List<UserQuizAnswer> answers = userQuizAnswerRepository.findAllByQuizId(quiz.getId());
            long total = answers.size();
            long correct = answers.stream().filter(UserQuizAnswer::getIsCorrect).count();
            double accuracy = total == 0 ? 100.0 : ((double) correct / total) * 100.0;

            QuizAccuracy qa = QuizAccuracy.builder()
                .id("quiz:" + quiz.getId())
                .quizId(quiz.getId())
                .categoryId(quiz.getCategory().getId())
                .accuracy(accuracy)
                .build();

            accuracyList.add(qa);
        }
        log.info("총 {}개의 정답률 캐싱 완료", accuracyList.size());
        quizAccuracyRedisRepository.saveAll(accuracyList);
    }
}
