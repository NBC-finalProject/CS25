package com.example.cs25service.domain.userQuizAnswer.service;

import com.example.cs25entity.domain.quiz.entity.Quiz;
import com.example.cs25entity.domain.quiz.entity.QuizCategory;
import com.example.cs25entity.domain.quiz.enums.QuizFormatType;
import com.example.cs25entity.domain.quiz.enums.QuizLevel;
import com.example.cs25entity.domain.quiz.exception.QuizException;
import com.example.cs25entity.domain.quiz.repository.QuizRepository;
import com.example.cs25entity.domain.subscription.entity.DayOfWeek;
import com.example.cs25entity.domain.subscription.entity.Subscription;
import com.example.cs25entity.domain.subscription.exception.SubscriptionException;
import com.example.cs25entity.domain.subscription.repository.SubscriptionRepository;
import com.example.cs25entity.domain.user.entity.Role;
import com.example.cs25entity.domain.user.entity.SocialType;
import com.example.cs25entity.domain.user.entity.User;
import com.example.cs25entity.domain.user.repository.UserRepository;
import com.example.cs25entity.domain.userQuizAnswer.dto.UserAnswerDto;
import com.example.cs25entity.domain.userQuizAnswer.entity.UserQuizAnswer;
import com.example.cs25entity.domain.userQuizAnswer.exception.UserQuizAnswerException;
import com.example.cs25entity.domain.userQuizAnswer.repository.UserQuizAnswerRepository;
import com.example.cs25service.domain.userQuizAnswer.dto.CheckSimpleAnswerResponseDto;
import com.example.cs25service.domain.userQuizAnswer.dto.SelectionRateResponseDto;
import com.example.cs25service.domain.userQuizAnswer.dto.UserQuizAnswerRequestDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.test.util.ReflectionTestUtils;

import javax.swing.text.html.Option;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Optional;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
    private UserQuizAnswer userQuizAnswer;
    private Quiz shortAnswerQuiz;
    private Quiz choiceQuiz;
    private User user;
    private UserQuizAnswerRequestDto requestDto;

    @BeforeEach
    void setUp() {
        QuizCategory category = QuizCategory.builder()
                .categoryType("BACKEND")
                .build();

        subscription = Subscription.builder()
                .category(category)
                .email("test@naver.com")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(1))
                .subscriptionType(EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY))
                .build();
        ReflectionTestUtils.setField(subscription, "id", 1L);
        ReflectionTestUtils.setField(subscription, "serialId", "sub-uuid-1");

        // 객관식 퀴즈
        choiceQuiz = Quiz.builder()
                .type(QuizFormatType.MULTIPLE_CHOICE)
                .question("Java is?")
                .answer("1. Programming Language")
                .commentary("Java is a language.")
                .choice("1. Programming // 2. Coffee")
                .category(category)
                .level(QuizLevel.EASY)
                .build();
        ReflectionTestUtils.setField(choiceQuiz, "id", 1L);
        ReflectionTestUtils.setField(choiceQuiz, "serialId", "sub-uuid-2");


        // 주관식 퀴즈
        shortAnswerQuiz = Quiz.builder()
                .type(QuizFormatType.SHORT_ANSWER)
                .question("Java is?")
                .answer("java")
                .commentary("Java is a language.")
                .category(category)
                .level(QuizLevel.EASY)
                .build();
        ReflectionTestUtils.setField(shortAnswerQuiz, "id", 1L);
        ReflectionTestUtils.setField(shortAnswerQuiz, "serialId", "sub-uuid-3");

        userQuizAnswer = UserQuizAnswer.builder()
                .userAnswer("1")
                .build();
        ReflectionTestUtils.setField(userQuizAnswer, "id", 1L);

        user = User.builder()
                .email("test@naver.com")
                .name("test")
                .role(Role.USER)
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);

        requestDto = new UserQuizAnswerRequestDto("1", subscription.getSerialId());
    }

    @Test
    void submitAnswer_정상_저장된다() {
        // given
        when(subscriptionRepository.findBySerialId(subscription.getSerialId())).thenReturn(Optional.of(subscription));
        when(quizRepository.findBySerialId(choiceQuiz.getSerialId())).thenReturn(Optional.of(choiceQuiz));
        when(userQuizAnswerRepository.existsByQuizIdAndSubscriptionId(choiceQuiz.getId(), subscription.getId())).thenReturn(false);
        when(userQuizAnswerRepository.save(any())).thenReturn(userQuizAnswer);

        // when
        Long answer = userQuizAnswerService.submitAnswer(choiceQuiz.getSerialId(), requestDto);

        // then

        assertThat(userQuizAnswer.getId()).isEqualTo(answer);
    }

    @Test
    void submitAnswer_구독없음_예외() {
        // given
        when(subscriptionRepository.findBySerialId(subscription.getSerialId())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userQuizAnswerService.submitAnswer(choiceQuiz.getSerialId(), requestDto))
                .isInstanceOf(SubscriptionException.class)
                .hasMessageContaining("구독 정보를 불러올 수 없습니다.");
    }

    @Test
    void submitAnswer_중복답변_예외(){
        //give
        when(subscriptionRepository.findBySerialId(subscription.getSerialId())).thenReturn(Optional.of(subscription));
        when(userQuizAnswerRepository.existsByQuizIdAndSubscriptionId(choiceQuiz.getId(), subscription.getId())).thenReturn(true);
        when(quizRepository.findBySerialId(choiceQuiz.getSerialId())).thenReturn(Optional.of(choiceQuiz));

        //when & then
        assertThatThrownBy(() -> userQuizAnswerService.submitAnswer(choiceQuiz.getSerialId(), requestDto))
                .isInstanceOf(UserQuizAnswerException.class)
                .hasMessageContaining("이미 제출한 문제입니다.");
    }

    @Test
    void submitAnswer_퀴즈없음_예외() {
        // given
        when(subscriptionRepository.findBySerialId(subscription.getSerialId())).thenReturn(Optional.of(subscription));
        when(quizRepository.findBySerialId(choiceQuiz.getSerialId())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userQuizAnswerService.submitAnswer(choiceQuiz.getSerialId(), requestDto))
                .isInstanceOf(QuizException.class)
                .hasMessageContaining("해당 퀴즈를 찾을 수 없습니다");
    }

    @Test
    void evaluateAnswer_비회원_객관식_정답(){
        //given
        UserQuizAnswer choiceAnswer = UserQuizAnswer.builder()
                .userAnswer("1")
                .quiz(choiceQuiz)
                .subscription(subscription)
                .build();

        when(userQuizAnswerRepository.findWithQuizAndUserById(choiceAnswer.getId())).thenReturn(Optional.of(choiceAnswer));

        //when
        CheckSimpleAnswerResponseDto checkSimpleAnswerResponseDto = userQuizAnswerService.evaluateAnswer(choiceAnswer.getId());

        //then
        assertThat(checkSimpleAnswerResponseDto.isCorrect()).isTrue();
    }

    @Test
    void evaluateAnswer_비회원_주관식_정답(){
        //given
        UserQuizAnswer shortAnswer = UserQuizAnswer.builder()
                .subscription(subscription)
                .userAnswer("java")
                .quiz(shortAnswerQuiz)
                .build();

        when(userQuizAnswerRepository.findWithQuizAndUserById(shortAnswer.getId())).thenReturn(Optional.of(shortAnswer));

        //when
        CheckSimpleAnswerResponseDto checkSimpleAnswerResponseDto = userQuizAnswerService.evaluateAnswer(shortAnswer.getId());

        //then
        assertThat(checkSimpleAnswerResponseDto.isCorrect()).isTrue();
    }

    @Test
    void evaluateAnswer_회원_객관식_정답_점수부여(){
        //given
        UserQuizAnswer choiceAnswer = UserQuizAnswer.builder()
                .userAnswer("1")
                .quiz(choiceQuiz)
                .user(user)
                .subscription(subscription)
                .build();

        when(userQuizAnswerRepository.findWithQuizAndUserById(choiceAnswer.getId())).thenReturn(Optional.of(choiceAnswer));

        //when
        CheckSimpleAnswerResponseDto checkSimpleAnswerResponseDto = userQuizAnswerService.evaluateAnswer(choiceAnswer.getId());

        //then
        assertThat(checkSimpleAnswerResponseDto.isCorrect()).isTrue();
        assertThat(user.getScore()).isEqualTo(3);
    }

    @Test
    void evaluateAnswer_회원_주관식_정답_점수부여(){
        //given
        UserQuizAnswer shortAnswer = UserQuizAnswer.builder()
                .subscription(subscription)
                .userAnswer("java")
                .user(user)
                .quiz(shortAnswerQuiz)
                .build();

        when(userQuizAnswerRepository.findWithQuizAndUserById(shortAnswer.getId())).thenReturn(Optional.of(shortAnswer));

        //when
        CheckSimpleAnswerResponseDto checkSimpleAnswerResponseDto = userQuizAnswerService.evaluateAnswer(shortAnswer.getId());

        //then
        assertThat(checkSimpleAnswerResponseDto.isCorrect()).isTrue();
        assertThat(user.getScore()).isEqualTo(9);
    }

    @Test
    void evaluateAnswer_오답(){
        //given
        UserQuizAnswer shortAnswer = UserQuizAnswer.builder()
                .subscription(subscription)
                .userAnswer("python")
                .quiz(shortAnswerQuiz)
                .build();

        when(userQuizAnswerRepository.findWithQuizAndUserById(shortAnswer.getId())).thenReturn(Optional.of(shortAnswer));

        //when
        CheckSimpleAnswerResponseDto checkSimpleAnswerResponseDto = userQuizAnswerService.evaluateAnswer(shortAnswer.getId());

        //then
        assertThat(checkSimpleAnswerResponseDto.isCorrect()).isFalse();
    }


    @Test
    void calculateSelectionRateByOption_조회_성공(){

        //given
        List<UserAnswerDto> answers = List.of(
                new UserAnswerDto("1"),
                new UserAnswerDto("1"),
                new UserAnswerDto("2"),
                new UserAnswerDto("2"),
                new UserAnswerDto("2"),
                new UserAnswerDto("3"),
                new UserAnswerDto("3"),
                new UserAnswerDto("3"),
                new UserAnswerDto("4"),
                new UserAnswerDto("4")
        );

        when(userQuizAnswerRepository.findUserAnswerByQuizId(choiceQuiz.getId())).thenReturn(answers);
        when(quizRepository.findBySerialId(choiceQuiz.getSerialId())).thenReturn(Optional.of(choiceQuiz));

        //when
        SelectionRateResponseDto selectionRateByOption = userQuizAnswerService.calculateSelectionRateByOption(choiceQuiz.getSerialId());

        //then
        assertThat(selectionRateByOption.getTotalCount()).isEqualTo(10);

        Map<String, Double> expectedRates = new HashMap<>();
        expectedRates.put("1", 2/10.0);
        expectedRates.put("2", 3/10.0);
        expectedRates.put("3", 3/10.0);
        expectedRates.put("4", 2/10.0);

        expectedRates.forEach((key, expectedRate) ->
                assertEquals(expectedRate, selectionRateByOption.getSelectionRates().get(key), 0.0001)
        );

    }
}