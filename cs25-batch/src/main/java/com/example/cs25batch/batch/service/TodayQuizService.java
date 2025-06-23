package com.example.cs25batch.batch.service;

import com.example.cs25batch.batch.dto.QuizDto;
import com.example.cs25entity.domain.quiz.entity.Quiz;
import com.example.cs25entity.domain.quiz.entity.QuizCategory;
import com.example.cs25entity.domain.quiz.enums.QuizFormatType;
import com.example.cs25entity.domain.quiz.enums.QuizLevel;
import com.example.cs25entity.domain.quiz.exception.QuizException;
import com.example.cs25entity.domain.quiz.exception.QuizExceptionCode;
import com.example.cs25entity.domain.quiz.repository.QuizRepository;
import com.example.cs25entity.domain.subscription.entity.Subscription;
import com.example.cs25entity.domain.subscription.repository.SubscriptionRepository;
import com.example.cs25entity.domain.userQuizAnswer.entity.UserQuizAnswer;
import com.example.cs25entity.domain.userQuizAnswer.repository.UserQuizAnswerRepository;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
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
    private final BatchMailService mailService;

    @Transactional
    public QuizDto getTodayQuiz(Long subscriptionId) {
        // 1. 구독자 정보 및 카테고리 조회
        Subscription subscription = subscriptionRepository.findByIdOrElseThrow(subscriptionId);
        Long parentCategoryId = subscription.getCategory().getId(); // 대분류 ID

        // 2. 유저 정답률 계산
        List<UserQuizAnswer> answerHistory = userQuizAnswerRepository.findByUserIdAndQuizCategoryId(
            subscriptionId, parentCategoryId);
        double accuracy = calculateAccuracy(answerHistory);

        // 3. 정답률 기반 난이도 바운더리 설정
        List<QuizLevel> allowedDifficulties = getAllowedDifficulties(accuracy);

        //4. 가장 최근에 푼 문제 소분류 카테고리 지워줘야해
        Set<Long> excludedCategoryIds = userQuizAnswerRepository.findRecentSolvedCategoryIds(
            subscriptionId,
            parentCategoryId,
            LocalDate.now().minusDays(1)  // ← 이거 몇일 중복 제거할건지 설정가능쓰
        );

        // 5. 내가 푼 문제 ID
        Set<Long> solvedQuizIds = answerHistory.stream()
            .map(a -> a.getQuiz().getId())
            .collect(Collectors.toSet());

        // 6. 서술형 주기 판단 (풀이 횟수 기반)
        int quizCount = answerHistory.size(); // 사용자가 지금까지 푼 문제 수
        boolean isEssayDay = quizCount % 5 == 4;

        List<QuizFormatType> targetTypes = isEssayDay
            ? List.of(QuizFormatType.SUBJECTIVE)
            : List.of(QuizFormatType.MULTIPLE_CHOICE, QuizFormatType.SHORT_ANSWER);

        // 7. 필터링 조건으로 문제 조회
        List<Quiz> candidateQuizzes = quizRepository.findAvailableQuizzesUnderParentCategory(
            parentCategoryId,
            allowedDifficulties,
            solvedQuizIds,
            excludedCategoryIds,
            targetTypes
        );

        if (candidateQuizzes.isEmpty()) {
            throw new QuizException(QuizExceptionCode.NO_QUIZ_EXISTS_ERROR);
        }

        // 8. 오프셋 계산 (풀이 수 기준)
        long offset = quizCount % candidateQuizzes.size();
        Quiz selectedQuiz = candidateQuizzes.get((int) offset);

        return QuizDto.builder()
            .id(selectedQuiz.getId())
            .quizCategory(selectedQuiz.getCategory().getCategoryType())
            .question(selectedQuiz.getQuestion())
            .choice(selectedQuiz.getChoice())
            .type(selectedQuiz.getType())
            .build();  //return -> QuizDto
    }

    //유저 정답률 기준으로 바운더리 정해줌
    private List<QuizLevel> getAllowedDifficulties(double accuracy) {
        // 난이도 낮
        if (accuracy <= 50.0) {
            return List.of(QuizLevel.EASY);
        } else if (accuracy <= 75.0) { //난이도 중
            return List.of(QuizLevel.EASY, QuizLevel.NORMAL);
        } else { //난이도 상
            return List.of(QuizLevel.EASY, QuizLevel.NORMAL, QuizLevel.HARD);
        }
    }
//
//    private long calculateOffset(Long subscriptionId, LocalDateTime createdAt, int size) {
//        long daysSince = ChronoUnit.DAYS.between(createdAt.toLocalDate(), LocalDate.now());
//        return (subscriptionId + daysSince) % size;
//    }


    @Transactional
    public Quiz getTodayQuizBySubscription(Subscription subscription) {
        //대분류 및 소분류 탐색
        List<QuizCategory> childCategories = subscription.getCategory().getChildren();
        List<Long> categoryIds = childCategories.stream()
            .map(QuizCategory::getId)
            .collect(Collectors.toList());

        categoryIds.add(subscription.getCategory().getId());

        //id 순으로 정렬
        List<Quiz> quizList = quizRepository.findAllByCategoryIdIn(categoryIds)
            .stream()
            .sorted(Comparator.comparing(Quiz::getId))  // id 순으로 정렬
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

//    @Transactional
//    public QuizDto getTodayQuizNew(Long subscriptionId) {
    /// ////////////////////여기는 구구 버전//////////////////////
    //        List<Quiz> quizList = quizRepository.findAllByCategoryId(
//                subscription.getCategory().getId())
//            .stream()
//            .sorted(Comparator.comparing(Quiz::getId))
//            .toList();
//
//
//        if (quizList.isEmpty()) {
//            throw new QuizException(QuizExceptionCode.NO_QUIZ_EXISTS_ERROR);
//        }
//
//        // 구독 시작일 기준 날짜 차이 계산
//        LocalDate createdDate = subscription.getCreatedAt().toLocalDate();
//        LocalDate today = LocalDate.now();
//        long daysSinceCreated = ChronoUnit.DAYS.between(createdDate, today);
//
//        // 슬라이딩 인덱스로 문제 선택
//        int offset = Math.toIntExact((subscriptionId + daysSinceCreated) % quizList.size());
//        Quiz selectedQuiz = quizList.get(offset);

    //return selectedQuiz;

    /// /////////////////////여기는 구버전 /////////////////////////////
//        //1. 해당 구독자의 문제 구독 카테고리 확인
//        Subscription subscription = subscriptionRepository.findByIdOrElseThrow(subscriptionId);
//        Long categoryId = subscription.getCategory().getId();
//
//        // 2. 유저의 정답률 계산
//        List<UserQuizAnswer> answers = userQuizAnswerRepository.findByUserIdAndCategoryId(
//            subscriptionId,
//            categoryId);
//        double userAccuracy = calculateAccuracy(answers); // 정답 수 / 전체 수
//
//        log.info("✳ getTodayQuizNew  유저의 정답률 계산 : {}", userAccuracy);
//        // 3. Redis에서 정답률 리스트 가져오기
//        List<QuizAccuracy> accuracyList = quizAccuracyRedisRepository.findAllByCategoryId(
//            categoryId);
//        //  QuizAccuracy 리스트를 Map<quizId, accuracy>로 변환
//        Map<Long, Double> quizAccuracyMap = accuracyList.stream()
//            .collect(Collectors.toMap(QuizAccuracy::getQuizId, QuizAccuracy::getAccuracy));
//
//        // 4. 유저가 푼 문제 ID 목록
//        Set<Long> solvedQuizIds = answers.stream()
//            .map(answer -> answer.getQuiz().getId())
//            .collect(Collectors.toSet());
//
//        // 5. 가장 비슷한 정답률을 가진 안푼 문제 찾기
//        Quiz selectedQuiz = quizAccuracyMap.entrySet().stream()
//            .filter(entry -> !solvedQuizIds.contains(entry.getKey()))
//            .min(Comparator.comparingDouble(entry -> Math.abs(entry.getValue() - userAccuracy)))
//            .flatMap(entry -> quizRepository.findById(entry.getKey()))
//            .orElse(null); // 없으면 null 또는 랜덤
//
//        if (selectedQuiz == null) {
//            throw new QuizException(QuizExceptionCode.NO_QUIZ_EXISTS_ERROR);
//        }
//        //return selectedQuiz;   //return -> Quiz
//        return QuizDto.builder()
//            .id(selectedQuiz.getId())
//            .quizCategory(selectedQuiz.getCategory().getCategoryType())
//            .question(selectedQuiz.getQuestion())
//            .choice(selectedQuiz.getChoice())
//            .type(selectedQuiz.getType())
//            .build(); //return -> QuizDto
//
//    }
    private double calculateAccuracy(List<UserQuizAnswer> answers) {
        if (answers.isEmpty()) {
            return 100.0;
        }

        int totalCorrect = 0;
        for (UserQuizAnswer answer : answers) {
            if (answer.getIsCorrect()) {
                totalCorrect++;
            }
        }
        return ((double) totalCorrect / answers.size()) * 100.0;
    }


    @Transactional
    public void issueTodayQuiz(Long subscriptionId) {
        //해당 구독자의 문제 구독 카테고리 확인
        Subscription subscription = subscriptionRepository.findByIdOrElseThrow(subscriptionId);
        //문제 발급
        Quiz selectedQuiz = getTodayQuizBySubscription(subscription);
        //메일 발송
        mailService.sendQuizEmail(subscription, selectedQuiz);
    }
}
