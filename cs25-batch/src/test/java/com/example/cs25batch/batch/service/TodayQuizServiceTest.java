package com.example.cs25batch.batch.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import com.example.cs25batch.batch.dto.QuizDto;
import com.example.cs25entity.domain.quiz.entity.Quiz;
import com.example.cs25entity.domain.quiz.entity.QuizCategory;
import com.example.cs25entity.domain.quiz.enums.QuizFormatType;
import com.example.cs25entity.domain.quiz.enums.QuizLevel;
import com.example.cs25entity.domain.quiz.repository.QuizRepository;
import com.example.cs25entity.domain.subscription.entity.DayOfWeek;
import com.example.cs25entity.domain.subscription.entity.Subscription;
import com.example.cs25entity.domain.subscription.repository.SubscriptionRepository;
import com.example.cs25entity.domain.userQuizAnswer.entity.UserQuizAnswer;
import com.example.cs25entity.domain.userQuizAnswer.repository.UserQuizAnswerRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TodayQuizServiceTest {

    @InjectMocks
    private TodayQuizService quizService;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private UserQuizAnswerRepository userQuizAnswerRepository;

    @Mock
    private QuizRepository quizRepository;

    Long parentCategoryId = 1L;
    private QuizCategory parentCategory;
    private List<QuizCategory> subCategories;

    @BeforeEach
    void setUp() {

        parentCategory = QuizCategory.builder()
            .categoryType("BACKEND")
            .parent(null)
            .build();

        ReflectionTestUtils.setField(parentCategory, "id", parentCategoryId);

        subCategories = new ArrayList<>();

        for (int i = 2; i < 7; i++) {
            QuizCategory subCategory = QuizCategory.builder()
                .categoryType("Subcategory" + (i - 1))
                .parent(parentCategory)
                .build();

            ReflectionTestUtils.setField(subCategory, "id", (long) (i)); // 2L부터 시작
            subCategories.add(subCategory);
        }
    }

    @Nested
    @DisplayName("TodayQuizV1")
    class getTodayQuizV1 {

        @Test
        @DisplayName(" getTodayQuiz 성공 - 조건 다있음")
        void getTodayQuiz_success() {
            // given
            Long subscriptionId = 1L;

            Subscription subscription = Subscription.builder()
                .category(parentCategory)
                .email("test@Test.com")
                .subscriptionType(Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
                    DayOfWeek.FRIDAY))
                .build();
            ReflectionTestUtils.setField(subscription, "id", subscriptionId);

            List<UserQuizAnswer> answerHistory = List.of(
                createAnswer(1L, QuizLevel.EASY, subCategories.get(4)),
                createAnswer(2L, QuizLevel.NORMAL, subCategories.get(4))
            );

            Set<Long> recentCategoryIds = Set.of(5L);
            Set<Long> solvedQuizIds = Set.of(1L, 2L);

            List<Quiz> availableQuizzes = List.of(
                createQuiz(3L, QuizFormatType.MULTIPLE_CHOICE, QuizLevel.HARD,
                    subCategories.get(0)),
                createQuiz(4L, QuizFormatType.SHORT_ANSWER, QuizLevel.EASY, subCategories.get(1)),
                createQuiz(5L, QuizFormatType.MULTIPLE_CHOICE, QuizLevel.NORMAL,
                    subCategories.get(2)),
                createQuiz(6L, QuizFormatType.SHORT_ANSWER, QuizLevel.EASY, subCategories.get(3))
            );

            given(subscriptionRepository.findByIdOrElseThrow(subscriptionId)).willReturn(
                subscription);
            given(userQuizAnswerRepository.findByUserIdAndQuizCategoryId(subscriptionId,
                parentCategoryId)).willReturn(answerHistory);
            given(userQuizAnswerRepository.findRecentSolvedCategoryIds(eq(subscriptionId),
                eq(parentCategoryId), any(
                    LocalDate.class)))
                .willReturn(recentCategoryIds);
            given(quizRepository.findAvailableQuizzesUnderParentCategory(eq(parentCategoryId),
                eq(List.of(QuizLevel.NORMAL, QuizLevel.EASY))
                , eq(solvedQuizIds)
                , eq(List.of(QuizFormatType.SHORT_ANSWER,
                    QuizFormatType.MULTIPLE_CHOICE)))).willReturn(
                availableQuizzes);

            //when
            QuizDto todayQuizDto = quizService.getTodayQuiz(subscriptionId);

            //then
            assertThat(todayQuizDto).isNotNull();
            assertThat(todayQuizDto.getId()).isEqualTo(
                5L); // offset = 2 % 4 = 2
            assertThat(todayQuizDto.getType()).isEqualTo(QuizFormatType.MULTIPLE_CHOICE);
        }

    }

    private UserQuizAnswer createAnswer(Long quizId, QuizLevel level, QuizCategory category) {
        Quiz quiz = Quiz.builder()
            .category(category)
            .level(level)
            .build();
        ReflectionTestUtils.setField(quiz, "id", quizId);

        return UserQuizAnswer.builder()
            .quiz(quiz)
            .isCorrect(true)
            .build();
    }

    private Quiz createQuiz(Long id, QuizFormatType type, QuizLevel level, QuizCategory category) {
        Quiz quiz = Quiz.builder()
            .type(type)
            .level(level)
            .category(category)
            .question("sample Question " + id)
            .choice("1. A // 2. B")
            .answer("1")
            .build();
        ReflectionTestUtils.setField(quiz, "id", id);

        return quiz;
    }

}
