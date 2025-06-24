package com.example.cs25service.domain.quiz.service;

import com.example.cs25entity.domain.quiz.entity.Quiz;
import com.example.cs25entity.domain.quiz.entity.QuizAccuracy;
import com.example.cs25entity.domain.quiz.enums.QuizFormatType;
import com.example.cs25entity.domain.quiz.enums.QuizLevel;
import com.example.cs25entity.domain.quiz.exception.QuizException;
import com.example.cs25entity.domain.quiz.exception.QuizExceptionCode;
import com.example.cs25entity.domain.quiz.repository.QuizAccuracyRedisRepository;
import com.example.cs25entity.domain.quiz.repository.QuizRepository;
import com.example.cs25entity.domain.subscription.entity.Subscription;
import com.example.cs25entity.domain.subscription.repository.SubscriptionRepository;
import com.example.cs25entity.domain.userQuizAnswer.entity.UserQuizAnswer;
import com.example.cs25entity.domain.userQuizAnswer.repository.UserQuizAnswerRepository;
import com.example.cs25service.domain.quiz.dto.test.QuizDto;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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
    private final SubscriptionRepository subscriptionRepository;


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


    @Transactional
    public QuizDto getTodayQuiz(Long subscriptionId) {
        // 1. 구독자 정보 및 카테고리 조회
        Subscription subscription = subscriptionRepository.findByIdOrElseThrow(subscriptionId);
        Long parentCategoryId = subscription.getCategory().getId(); // 대분류 ID

        // 2. 유저 정답률 계산
        List<UserQuizAnswer> answerHistory = userQuizAnswerRepository.findByUserIdAndQuizCategoryId(
            subscriptionId, parentCategoryId);
        double accuracy = calculateAccuracy(answerHistory);

        //4. 가장 최근에 푼 문제 소분류 카테고리 지워줘야해
        Set<Long> excludedCategoryIds = userQuizAnswerRepository.findRecentSolvedCategoryIds(
            subscriptionId,
            parentCategoryId,
            LocalDate.now().minusDays(1)  // 이거 몇일 중복 제거할건지 설정가능쓰
        );

        // 5. 내가 푼 문제 ID
        Set<Long> solvedQuizIds = answerHistory.stream()
            .map(a -> a.getQuiz().getId())
            .collect(Collectors.toSet());

        // 6. 서술형 주기 판단 (풀이 횟수 기반)
        int quizCount = answerHistory.size(); // 사용자가 지금까지 푼 문제 수
        boolean isEssayDay = quizCount % 5 == 4; //일단 5배수일때 한번씩은 서술 뽑아줘야함( 조정 필요하면 나중에 하는거롤)

        List<QuizFormatType> targetTypes = isEssayDay
            ? List.of(QuizFormatType.SUBJECTIVE)
            : List.of(QuizFormatType.MULTIPLE_CHOICE, QuizFormatType.SHORT_ANSWER);

        // 3. 정답률 기반 난이도 바운더리 설정
        List<QuizLevel> allowedDifficulties = getAllowedDifficulties(accuracy);

        // 7. 필터링 조건으로 문제 조회(대분류, 난이도, 내가푼문제 제외, 제외할 카테고리 제외하고, 문제 타입 전부 조건으로)
        List<Quiz> candidateQuizzes = quizRepository.findAvailableQuizzesUnderParentCategory(
            parentCategoryId,
            allowedDifficulties,
            solvedQuizIds,
            excludedCategoryIds,
            targetTypes
        ); //한개만뽑기(find first)

        if (candidateQuizzes.isEmpty()) { // 뽀ㅃ을문제없을때
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


}
