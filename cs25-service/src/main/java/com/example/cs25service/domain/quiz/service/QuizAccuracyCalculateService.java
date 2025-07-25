package com.example.cs25service.domain.quiz.service;

import com.example.cs25entity.domain.mail.repository.MailLogRepository;
import com.example.cs25entity.domain.quiz.entity.Quiz;
import com.example.cs25entity.domain.quiz.entity.QuizAccuracy;
import com.example.cs25entity.domain.quiz.enums.QuizFormatType;
import com.example.cs25entity.domain.quiz.enums.QuizLevel;
import com.example.cs25entity.domain.quiz.exception.QuizException;
import com.example.cs25entity.domain.quiz.exception.QuizExceptionCode;
import com.example.cs25entity.domain.quiz.repository.QuizAccuracyRedisRepository;
import com.example.cs25entity.domain.quiz.repository.QuizRepository;
import com.example.cs25entity.domain.subscription.entity.Subscription;
import com.example.cs25entity.domain.userQuizAnswer.entity.UserQuizAnswer;
import com.example.cs25entity.domain.userQuizAnswer.repository.UserQuizAnswerRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class QuizAccuracyCalculateService {

    private final QuizRepository quizRepository;
    private final QuizAccuracyRedisRepository quizAccuracyRedisRepository;
    private final UserQuizAnswerRepository userQuizAnswerRepository;
    private final MailLogRepository mailLogRepository;

    @Transactional
    public Quiz getTodayQuizBySubscription(Subscription subscription) {
        // 1. 구독자 정보 및 카테고리 조회
        Long parentCategoryId = subscription.getCategory().getId(); // 대분류 ID
        Long subscriptionId = subscription.getId();

        // 2. 유저 정답률 계산, 내가 푼 문제 아이디값
        Double accuracyResult = userQuizAnswerRepository.getCorrectRate(subscriptionId,
            parentCategoryId);
        double accuracy = accuracyResult != null ? accuracyResult : 100.0;

        Set<Long> sentQuizIds = mailLogRepository.findQuiz_IdBySubscription_Id(subscriptionId);
        int quizCount = sentQuizIds.size(); // 사용자가 지금까지 푼 문제 수

        // 6. 서술형 주기 판단 (풀이 횟수 기반)
        boolean isEssayDay = quizCount % 4 == 3; //일단 3배수일때 한번씩은 서술(0,1,2 객관식 / 3서술형)

        QuizFormatType targetType = isEssayDay
            ? QuizFormatType.SUBJECTIVE
            : QuizFormatType.MULTIPLE_CHOICE;

        // 3. 정답률 기반 난이도 바운더리 설정
        List<QuizLevel> allowedDifficulties = getAllowedDifficulties(accuracy);

        // 8. 오프셋 계산 (풀이 수 기준)
        long seed = LocalDate.now().toEpochDay() + subscriptionId;
        int offset = (int) (seed % 20);

        // 7. 필터링 조건으로 문제 조회(대분류, 난이도, 내가푼문제 제외, 제외할 카테고리 제외하고, 문제 타입 전부 조건으로)

        Quiz todayQuiz = quizRepository.findAvailableQuizzesUnderParentCategory(
            parentCategoryId,
            allowedDifficulties,
            sentQuizIds,
            //excludedCategoryIds,
            targetType,
            offset
        );

        if (todayQuiz == null) {
            throw new QuizException(QuizExceptionCode.QUIZ_VALIDATION_FAILED_ERROR);
        }

        return todayQuiz;
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
