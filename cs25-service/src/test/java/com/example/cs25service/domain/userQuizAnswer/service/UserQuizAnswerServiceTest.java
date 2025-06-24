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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;

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
    private final Long quizId = 1L;
    private final String serialId = "uuid";

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

        // 객관식 퀴즈
        choiceQuiz = Quiz.builder()
                .type(QuizFormatType.MULTIPLE_CHOICE)
                .question("Java is?")
                .answer("1. Programming Language")
                .commentary("Java is a language.")
                .choice("1. Programming // 2. Coffee")
                .category(category)
                .type(QuizFormatType.MULTIPLE_CHOICE)
                .level(QuizLevel.EASY)
                .build();

        // 주관식 퀴즈
        shortAnswerQuiz = Quiz.builder()
                .type(QuizFormatType.MULTIPLE_CHOICE)
                .question("Java is?")
                .answer("java")
                .commentary("Java is a language.")
                .category(category)
                .type(QuizFormatType.SHORT_ANSWER)
                .level(QuizLevel.EASY)
                .build();

        userQuizAnswer = UserQuizAnswer.builder()
                .userAnswer("1")
                .build();

        user = User.builder()
                .email("test@naver.com")
                .name("test")
                .role(Role.USER)
                .build();

        requestDto = new UserQuizAnswerRequestDto("1", serialId);
    }

    @Test
    void answerSubmit_정상_저장된다() {
        // given
        when(subscriptionRepository.findBySerialId(serialId)).thenReturn(Optional.of(subscription));
        when(quizRepository.findById(quizId)).thenReturn(Optional.of(choiceQuiz));
        when(userQuizAnswerRepository.existsByQuizIdAndSubscriptionId(quizId, subscription.getId())).thenReturn(false);
        when(userQuizAnswerRepository.save(any())).thenReturn(userQuizAnswer);

        // when
        Long answer = userQuizAnswerService.answerSubmit(quizId, requestDto);

        // then

        assertThat(userQuizAnswer.getId()).isEqualTo(answer);
    }

    @Test
    void answerSubmit_구독없음_예외() {
        // given
        when(subscriptionRepository.findBySerialId(serialId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userQuizAnswerService.answerSubmit(quizId, requestDto))
                .isInstanceOf(SubscriptionException.class)
                .hasMessageContaining("구독 정보를 불러올 수 없습니다.");
    }

    @Test
    void answerSubmit_중복답변_예외(){
        //give
        when(subscriptionRepository.findBySerialId(serialId)).thenReturn(Optional.of(subscription));
        when(userQuizAnswerRepository.existsByQuizIdAndSubscriptionId(quizId, subscription.getId())).thenReturn(true);

        //when & then
        assertThatThrownBy(() -> userQuizAnswerService.answerSubmit(quizId, requestDto))
                .isInstanceOf(UserQuizAnswerException.class)
                .hasMessageContaining("이미 제출한 문제입니다.");
    }

    @Test
    void answerSubmit_퀴즈없음_예외() {
        // given
        when(subscriptionRepository.findBySerialId(serialId)).thenReturn(Optional.of(subscription));
        when(quizRepository.findById(quizId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userQuizAnswerService.answerSubmit(quizId, requestDto))
                .isInstanceOf(QuizException.class)
                .hasMessageContaining("해당 퀴즈를 찾을 수 없습니다");
    }

    @Test
    void checkSimpleAnswer_비회원_객관식_정답(){
        //given
        UserQuizAnswer choiceAnswer = UserQuizAnswer.builder()
                .userAnswer("1")
                .quiz(choiceQuiz)
                .subscription(subscription)
                .build();

        when(userQuizAnswerRepository.findByIdWithQuiz(choiceAnswer.getId())).thenReturn(Optional.of(choiceAnswer));
        when(quizRepository.findById(choiceAnswer.getQuiz().getId())).thenReturn(Optional.of(choiceQuiz));

        //when
        CheckSimpleAnswerResponseDto checkSimpleAnswerResponseDto = userQuizAnswerService.checkSimpleAnswer(choiceAnswer.getId());

        //then
        assertThat(checkSimpleAnswerResponseDto.isCorrect()).isTrue();
    }

    @Test
    void checkSimpleAnswer_비회원_주관식_정답(){
        //given
        UserQuizAnswer shortAnswer = UserQuizAnswer.builder()
                .subscription(subscription)
                .userAnswer("java")
                .quiz(shortAnswerQuiz)
                .build();

        when(userQuizAnswerRepository.findByIdWithQuiz(shortAnswer.getId())).thenReturn(Optional.of(shortAnswer));
        when(quizRepository.findById(shortAnswer.getQuiz().getId())).thenReturn(Optional.of(shortAnswerQuiz));

        //when
        CheckSimpleAnswerResponseDto checkSimpleAnswerResponseDto = userQuizAnswerService.checkSimpleAnswer(shortAnswer.getId());

        //then
        assertThat(checkSimpleAnswerResponseDto.isCorrect()).isTrue();
    }

    @Test
    void checkSimpleAnswer_회원_객관식_정답_점수부여(){
        //given
        UserQuizAnswer choiceAnswer = UserQuizAnswer.builder()
                .userAnswer("1")
                .quiz(choiceQuiz)
                .user(user)
                .subscription(subscription)
                .build();

        when(userQuizAnswerRepository.findByIdWithQuiz(choiceAnswer.getId())).thenReturn(Optional.of(choiceAnswer));
        when(quizRepository.findById(choiceAnswer.getQuiz().getId())).thenReturn(Optional.of(choiceQuiz));
        when(userRepository.findBySubscription(subscription)).thenReturn(Optional.of(user));

        //when
        CheckSimpleAnswerResponseDto checkSimpleAnswerResponseDto = userQuizAnswerService.checkSimpleAnswer(choiceAnswer.getId());

        //then
        assertThat(checkSimpleAnswerResponseDto.isCorrect()).isTrue();
        assertThat(user.getScore()).isEqualTo(3);
    }

    @Test
    void checkSimpleAnswer_회원_주관식_정답_점수부여(){
        //given
        UserQuizAnswer shortAnswer = UserQuizAnswer.builder()
                .subscription(subscription)
                .userAnswer("java")
                .quiz(shortAnswerQuiz)
                .build();

        when(userQuizAnswerRepository.findByIdWithQuiz(shortAnswer.getId())).thenReturn(Optional.of(shortAnswer));
        when(quizRepository.findById(shortAnswer.getQuiz().getId())).thenReturn(Optional.of(shortAnswerQuiz));
        when(userRepository.findBySubscription(subscription)).thenReturn(Optional.of(user));

        //when
        CheckSimpleAnswerResponseDto checkSimpleAnswerResponseDto = userQuizAnswerService.checkSimpleAnswer(shortAnswer.getId());

        //then
        assertThat(checkSimpleAnswerResponseDto.isCorrect()).isTrue();
        assertThat(user.getScore()).isEqualTo(9);
    }

    @Test
    void checkSimpleAnswer_오답(){
        //given
        UserQuizAnswer shortAnswer = UserQuizAnswer.builder()
                .subscription(subscription)
                .userAnswer("python")
                .quiz(shortAnswerQuiz)
                .build();

        when(userQuizAnswerRepository.findByIdWithQuiz(shortAnswer.getId())).thenReturn(Optional.of(shortAnswer));
        when(quizRepository.findById(shortAnswer.getQuiz().getId())).thenReturn(Optional.of(shortAnswerQuiz));

        //when
        CheckSimpleAnswerResponseDto checkSimpleAnswerResponseDto = userQuizAnswerService.checkSimpleAnswer(shortAnswer.getId());

        //then
        assertThat(checkSimpleAnswerResponseDto.isCorrect()).isFalse();
    }


    @Test
    void getSelectionRateByOption_조회_성공(){

        //given
        Long quizId = 1L;
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

        when(userQuizAnswerRepository.findUserAnswerByQuizId(quizId)).thenReturn(answers);

        //when
        SelectionRateResponseDto selectionRateByOption = userQuizAnswerService.getSelectionRateByOption(quizId);

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