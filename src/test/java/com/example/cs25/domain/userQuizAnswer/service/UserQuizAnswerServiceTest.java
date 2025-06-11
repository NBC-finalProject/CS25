package com.example.cs25.domain.userQuizAnswer.service;

import com.example.cs25.domain.oauth2.dto.SocialType;
import com.example.cs25.domain.quiz.entity.Quiz;
import com.example.cs25.domain.quiz.entity.QuizCategory;
import com.example.cs25.domain.quiz.entity.QuizFormatType;
import com.example.cs25.domain.quiz.exception.QuizException;
import com.example.cs25.domain.quiz.repository.QuizRepository;
import com.example.cs25.domain.subscription.dto.SubscriptionRequest;
import com.example.cs25.domain.subscription.entity.DayOfWeek;
import com.example.cs25.domain.subscription.entity.Subscription;
import com.example.cs25.domain.subscription.exception.SubscriptionException;
import com.example.cs25.domain.subscription.exception.SubscriptionExceptionCode;
import com.example.cs25.domain.subscription.repository.SubscriptionRepository;
import com.example.cs25.domain.subscription.service.SubscriptionService;
import com.example.cs25.domain.userQuizAnswer.entity.UserQuizAnswer;
import com.example.cs25.domain.userQuizAnswer.repository.UserQuizAnswerRepository;
import com.example.cs25.domain.userQuizAnswer.requestDto.UserQuizAnswerRequestDto;
import com.example.cs25.domain.users.entity.Role;
import com.example.cs25.domain.users.entity.User;
import com.example.cs25.domain.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserQuizAnswerServiceTest {

    @InjectMocks
    private UserQuizAnswerService userQuizAnswerService;

    @Mock
    private UserQuizAnswerRepository userQuizAnswerRepository;

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    private Subscription subscription;
    private User user;
    private Quiz quiz;
    private UserQuizAnswerRequestDto requestDto;
    private final Long quizId = 1L;
    private final Long subscriptionId = 100L;

    @BeforeEach
    void setUp() {
        QuizCategory category = QuizCategory.builder()
                .categoryType("BECKEND")
                .build();

        subscription = Subscription.builder()
                .category(category)
                .email("test@naver.com")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(1))
                .subscriptionType(EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY))
                .build();

        user = User.builder()
                .email("user@naver.com")
                .name("김테스터")
                .socialType(SocialType.KAKAO)
                .role(Role.USER)
                .subscription(subscription)
                .build();

        quiz = Quiz.builder()
                .type(QuizFormatType.MULTIPLE_CHOICE)
                .question("Java is?")
                .answer("1. Programming Language")
                .commentary("Java is a language.")
                .choice("1. Programming // 2. Coffee")
                .category(category)
                .build();

        requestDto = UserQuizAnswerRequestDto.builder()
                .subscriptionId(subscriptionId)
                .answer("1")
                .build();
    }

    @Test
    void answerSubmit_정상_저장된다() {
        // given
        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(subscription));
        when(userRepository.findBySubscription(subscription)).thenReturn(user);
        when(quizRepository.findById(quizId)).thenReturn(Optional.of(quiz));

        ArgumentCaptor<UserQuizAnswer> captor = ArgumentCaptor.forClass(UserQuizAnswer.class);

        // when
        userQuizAnswerService.answerSubmit(quizId, requestDto);

        // then
        verify(userQuizAnswerRepository).save(captor.capture());
        UserQuizAnswer saved = captor.getValue();

        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getQuiz()).isEqualTo(quiz);
        assertThat(saved.getSubscription()).isEqualTo(subscription);
        assertThat(saved.getUserAnswer()).isEqualTo("1");
        assertThat(saved.getIsCorrect()).isTrue();
    }

    @Test
    void answerSubmit_구독없음_예외() {
        // given
        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userQuizAnswerService.answerSubmit(quizId, requestDto))
                .isInstanceOf(SubscriptionException.class)
                .hasMessageContaining("구독 정보를 불러올 수 없습니다.");
    }

    @Test
    void answerSubmit_퀴즈없음_예외() {
        // given
        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(subscription));
        when(userRepository.findBySubscription(subscription)).thenReturn(user);
        when(quizRepository.findById(quizId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userQuizAnswerService.answerSubmit(quizId, requestDto))
                .isInstanceOf(QuizException.class)
                .hasMessageContaining("해당 퀴즈를 찾을 수 없습니다");
    }
}