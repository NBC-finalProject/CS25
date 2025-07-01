package com.example.cs25entity.domain.quiz.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QuizAccuracyTest {

    private Long testQuizId;
    private Long testCategoryId;
    private double testAccuracy;
    private String testId;

    @BeforeEach
    void setUp() {
        testQuizId = 123L;
        testCategoryId = 45L;
        testAccuracy = 85.5;
        testId = "quiz:" + testQuizId + ":category:" + testCategoryId;
    }

    @Test
    @DisplayName("QuizAccuracy 빌더 패턴으로 객체 생성 테스트")
    void createQuizAccuracyWithBuilder() {
        // when
        QuizAccuracy quizAccuracy = QuizAccuracy.builder()
            .id(testId)
            .quizId(testQuizId)
            .categoryId(testCategoryId)
            .accuracy(testAccuracy)
            .build();

        // then
        assertThat(quizAccuracy).isNotNull();
        assertThat(quizAccuracy.getId()).isEqualTo(testId);
        assertThat(quizAccuracy.getQuizId()).isEqualTo(testQuizId);
        assertThat(quizAccuracy.getCategoryId()).isEqualTo(testCategoryId);
        assertThat(quizAccuracy.getAccuracy()).isEqualTo(testAccuracy);
    }

    @Test
    @DisplayName("QuizAccuracy 기본 생성자로 객체 생성 테스트")
    void createQuizAccuracyWithNoArgsConstructor() {
        // when
        QuizAccuracy quizAccuracy = new QuizAccuracy();

        // then
        assertThat(quizAccuracy).isNotNull();
        assertThat(quizAccuracy.getId()).isNull();
        assertThat(quizAccuracy.getQuizId()).isNull();
        assertThat(quizAccuracy.getCategoryId()).isNull();
        assertThat(quizAccuracy.getAccuracy()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("QuizAccuracy ID 생성 패턴 테스트")
    void validateIdPattern() {
        // given
        Long quizId1 = 1L;
        Long categoryId1 = 10L;
        Long quizId2 = 999L;
        Long categoryId2 = 123L;

        // when
        QuizAccuracy accuracy1 = QuizAccuracy.builder()
            .id("quiz:" + quizId1 + ":category:" + categoryId1)
            .quizId(quizId1)
            .categoryId(categoryId1)
            .accuracy(90.0)
            .build();

        QuizAccuracy accuracy2 = QuizAccuracy.builder()
            .id("quiz:" + quizId2 + ":category:" + categoryId2)
            .quizId(quizId2)
            .categoryId(categoryId2)
            .accuracy(75.5)
            .build();

        // then
        assertThat(accuracy1.getId()).isEqualTo("quiz:1:category:10");
        assertThat(accuracy2.getId()).isEqualTo("quiz:999:category:123");
    }

    @Test
    @DisplayName("정확도 범위 테스트 - 0% ~ 100%")
    void validateAccuracyRange() {
        // given
        double minAccuracy = 0.0;
        double maxAccuracy = 100.0;
        double middleAccuracy = 50.5;

        // when
        QuizAccuracy minAccuracyQuiz = QuizAccuracy.builder()
            .id("quiz:1:category:1")
            .quizId(1L)
            .categoryId(1L)
            .accuracy(minAccuracy)
            .build();

        QuizAccuracy maxAccuracyQuiz = QuizAccuracy.builder()
            .id("quiz:2:category:1")
            .quizId(2L)
            .categoryId(1L)
            .accuracy(maxAccuracy)
            .build();

        QuizAccuracy middleAccuracyQuiz = QuizAccuracy.builder()
            .id("quiz:3:category:1")
            .quizId(3L)
            .categoryId(1L)
            .accuracy(middleAccuracy)
            .build();

        // then
        assertThat(minAccuracyQuiz.getAccuracy()).isEqualTo(0.0);
        assertThat(maxAccuracyQuiz.getAccuracy()).isEqualTo(100.0);
        assertThat(middleAccuracyQuiz.getAccuracy()).isEqualTo(50.5);
    }

    @Test
    @DisplayName("같은 카테고리의 다른 퀴즈 정확도 테스트")
    void validateSameCategoryDifferentQuizzes() {
        // given
        Long categoryId = 10L;
        Long quizId1 = 1L;
        Long quizId2 = 2L;
        double accuracy1 = 80.0;
        double accuracy2 = 95.0;

        // when
        QuizAccuracy quiz1Accuracy = QuizAccuracy.builder()
            .id("quiz:" + quizId1 + ":category:" + categoryId)
            .quizId(quizId1)
            .categoryId(categoryId)
            .accuracy(accuracy1)
            .build();

        QuizAccuracy quiz2Accuracy = QuizAccuracy.builder()
            .id("quiz:" + quizId2 + ":category:" + categoryId)
            .quizId(quizId2)
            .categoryId(categoryId)
            .accuracy(accuracy2)
            .build();

        // then
        assertThat(quiz1Accuracy.getCategoryId()).isEqualTo(quiz2Accuracy.getCategoryId());
        assertThat(quiz1Accuracy.getQuizId()).isNotEqualTo(quiz2Accuracy.getQuizId());
        assertThat(quiz1Accuracy.getAccuracy()).isNotEqualTo(quiz2Accuracy.getAccuracy());
        assertThat(quiz1Accuracy.getId()).isNotEqualTo(quiz2Accuracy.getId());
    }

    @Test
    @DisplayName("소수점 정확도 값 테스트")
    void validateDecimalAccuracyValues() {
        // given
        double[] accuracyValues = {85.5, 92.75, 67.123, 100.0, 0.001};

        for (int i = 0; i < accuracyValues.length; i++) {
            // when
            QuizAccuracy quizAccuracy = QuizAccuracy.builder()
                .id("quiz:" + (i + 1) + ":category:1")
                .quizId((long) (i + 1))
                .categoryId(1L)
                .accuracy(accuracyValues[i])
                .build();

            // then
            assertThat(quizAccuracy.getAccuracy()).isEqualTo(accuracyValues[i]);
        }
    }

    @Test
    @DisplayName("QuizAccuracy 객체 동등성 테스트")
    void validateObjectEquality() {
        // given
        QuizAccuracy accuracy1 = QuizAccuracy.builder()
            .id(testId)
            .quizId(testQuizId)
            .categoryId(testCategoryId)
            .accuracy(testAccuracy)
            .build();

        QuizAccuracy accuracy2 = QuizAccuracy.builder()
            .id(testId)
            .quizId(testQuizId)
            .categoryId(testCategoryId)
            .accuracy(testAccuracy)
            .build();

        // when & then
        assertThat(accuracy1).isNotSameAs(accuracy2);
        assertThat(accuracy1.getId()).isEqualTo(accuracy2.getId());
        assertThat(accuracy1.getQuizId()).isEqualTo(accuracy2.getQuizId());
        assertThat(accuracy1.getCategoryId()).isEqualTo(accuracy2.getCategoryId());
        assertThat(accuracy1.getAccuracy()).isEqualTo(accuracy2.getAccuracy());
    }

    @Test
    @DisplayName("null 값 처리 테스트")
    void validateNullValueHandling() {
        // when
        QuizAccuracy quizAccuracy = QuizAccuracy.builder()
            .id(null)
            .quizId(null)
            .categoryId(null)
            .accuracy(75.0)
            .build();

        // then
        assertThat(quizAccuracy.getId()).isNull();
        assertThat(quizAccuracy.getQuizId()).isNull();
        assertThat(quizAccuracy.getCategoryId()).isNull();
        assertThat(quizAccuracy.getAccuracy()).isEqualTo(75.0);
    }
}