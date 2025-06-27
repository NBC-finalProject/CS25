package com.example.cs25.domain.quiz.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.example.cs25.domain.quiz.dto.QuizDto;
import com.example.cs25.domain.quiz.entity.Quiz;
import com.example.cs25.domain.quiz.entity.QuizAccuracy;
import com.example.cs25.domain.quiz.entity.QuizCategory;
import com.example.cs25.domain.quiz.entity.QuizFormatType;
import com.example.cs25.domain.quiz.exception.QuizException;
import com.example.cs25.domain.quiz.repository.QuizAccuracyRedisRepository;
import com.example.cs25.domain.quiz.repository.QuizRepository;
import com.example.cs25.domain.subscription.entity.DayOfWeek;
import com.example.cs25.domain.subscription.entity.Subscription;
import com.example.cs25.domain.subscription.repository.SubscriptionRepository;
import com.example.cs25.domain.userQuizAnswer.entity.UserQuizAnswer;
import com.example.cs25.domain.userQuizAnswer.repository.UserQuizAnswerRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TodayQuizServiceTest {

    @InjectMocks
    private TodayQuizService todayQuizService;

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private UserQuizAnswerRepository userQuizAnswerRepository;

    @Mock
    private QuizAccuracyRedisRepository quizAccuracyRedisRepository;

    private Long subscriptionId = 1L;
    private Subscription subscription;

    @BeforeEach
    void setUp() {
        subscription = Subscription.builder()
            .subscriptionType(Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY))
            .startDate(LocalDate.of(2025, 1, 1))
            .endDate(LocalDate.of(2026, 1, 1))
            .category(new QuizCategory(1L, "BACKEND"))
            .build();

        ReflectionTestUtils.setField(subscription, "id", subscriptionId);
    }

    @Test
    void getTodayQuiz_성공() {
        // given
        LocalDate createdAt = LocalDate.now().minusDays(5);
        ReflectionTestUtils.setField(subscription, "createdAt", createdAt.atStartOfDay());

        // given
        Quiz quiz1 = Quiz.builder()
            .category(new QuizCategory(1L, "BACKEND"))
            .question("자바에서 List와 Set의 차이는?")
            .choice("1.중복 허용 여부/2.순서 보장 여부")
            .type(QuizFormatType.MULTIPLE_CHOICE)
            .build();
        ReflectionTestUtils.setField(quiz1, "id", 10L);

        Quiz quiz2 = Quiz.builder()
            .category(new QuizCategory(1L, "BACKEND"))
            .question(
                "유스케이스(Use Case)의 구성 요소 간의 관계에 포함되지 않는 것은?")
            .choice("1.연관/2.확장/3.구체화/4.일반화/")
            .type(QuizFormatType.MULTIPLE_CHOICE)
            .build();
        ReflectionTestUtils.setField(quiz2, "id", 11L);

        List<Quiz> quizzes = List.of(quiz1, quiz2);

        given(subscriptionRepository.findByIdOrElseThrow(subscriptionId)).willReturn(subscription);
        given(quizRepository.findAllByCategoryId(1L)).willReturn(quizzes);

        // when
        QuizDto result = todayQuizService.getTodayQuiz(subscriptionId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getQuizCategory()).isEqualTo("BACKEND");
        assertThat(result.getChoice()).isEqualTo("1.중복 허용 여부/2.순서 보장 여부");
    }

    @Test
    void getTodayQuiz_낼_문제가_없으면_오류() {
        // given
        ReflectionTestUtils.setField(subscription, "createdAt", LocalDate.now().atStartOfDay());

        given(subscriptionRepository.findByIdOrElseThrow(subscriptionId)).willReturn(subscription);
        given(quizRepository.findAllByCategoryId(1L)).willReturn(List.of());

        // when & then
        assertThatThrownBy(() -> todayQuizService.getTodayQuiz(subscriptionId))
            .isInstanceOf(QuizException.class)
            .hasMessageContaining("해당 카테고리에 문제가 없습니다.");
    }


    @Test
    void getTodayQuizNew_낼_문제가_없으면_오류() {
        // given
        given(subscriptionRepository.findByIdOrElseThrow(subscriptionId))
            .willReturn(subscription);

        given(userQuizAnswerRepository.findByUserIdAndCategoryId(subscriptionId, 1L))
            .willReturn(List.of());

        given(quizAccuracyRedisRepository.findAllByCategoryId(1L))
            .willReturn(List.of());

        // when & then
        assertThatThrownBy(() -> todayQuizService.getTodayQuizNew(subscriptionId))
            .isInstanceOf(QuizException.class)
            .hasMessage("해당 카테고리에 문제가 없습니다.");
    }

    @Test
    void getTodayQuizNew_성공() {
        // given
        Quiz quiz = Quiz.builder()
            .category(new QuizCategory(1L, "BACKEND"))
            .question("자바에서 List와 Set의 차이는?")
            .choice("1.중복 허용 여부/2.순서 보장 여부")
            .type(QuizFormatType.MULTIPLE_CHOICE)
            .build();
        ReflectionTestUtils.setField(quiz, "id", 10L);

        Quiz quiz1 = Quiz.builder()
            .category(new QuizCategory(1L, "BACKEND"))
            .question(
                "유스케이스(Use Case)의 구성 요소 간의 관계에 포함되지 않는 것은?")
            .choice("1.연관/2.확장/3.구체화/4.일반화/")
            .type(QuizFormatType.MULTIPLE_CHOICE)
            .build();
        ReflectionTestUtils.setField(quiz1, "id", 11L);

        UserQuizAnswer userQuizAnswer = UserQuizAnswer.builder()
            .quiz(quiz)
            .isCorrect(true)
            .build();

        QuizAccuracy quizAccuracy = QuizAccuracy.builder()
            .quizId(10L)
            .categoryId(1L)
            .accuracy(90.0)
            .build();

        QuizAccuracy quizAccuracy1 = QuizAccuracy.builder()
            .quizId(11L)
            .categoryId(1L)
            .accuracy(85.0)
            .build();

        given(subscriptionRepository.findByIdOrElseThrow(subscriptionId))
            .willReturn(subscription);

        given(userQuizAnswerRepository.findByUserIdAndCategoryId(subscriptionId, 1L))
            .willReturn(List.of(userQuizAnswer));

        given(quizAccuracyRedisRepository.findAllByCategoryId(1L))
            .willReturn(List.of(quizAccuracy, quizAccuracy1));

        given(quizRepository.findById(11L))
            .willReturn(Optional.of(quiz));

        // when
        QuizDto result = todayQuizService.getTodayQuizNew(subscriptionId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getQuestion()).isEqualTo("자바에서 List와 Set의 차이는?");
    }

}