package com.example.cs25service.domain.userQuizAnswer.service;

import com.example.cs25entity.domain.quiz.entity.Quiz;
import com.example.cs25entity.domain.quiz.entity.QuizCategory;
import com.example.cs25entity.domain.quiz.enums.QuizFormatType;
import com.example.cs25entity.domain.quiz.enums.QuizLevel;
import com.example.cs25entity.domain.quiz.exception.QuizException;
import com.example.cs25entity.domain.quiz.exception.QuizExceptionCode;
import com.example.cs25entity.domain.quiz.repository.QuizRepository;
import com.example.cs25entity.domain.subscription.entity.DayOfWeek;
import com.example.cs25entity.domain.subscription.entity.Subscription;
import com.example.cs25entity.domain.subscription.exception.SubscriptionException;
import com.example.cs25entity.domain.subscription.exception.SubscriptionExceptionCode;
import com.example.cs25entity.domain.subscription.repository.SubscriptionRepository;
import com.example.cs25entity.domain.user.entity.Role;
import com.example.cs25entity.domain.user.entity.User;
import com.example.cs25entity.domain.user.repository.UserRepository;
import com.example.cs25entity.domain.userQuizAnswer.dto.UserAnswerDto;
import com.example.cs25entity.domain.userQuizAnswer.entity.UserQuizAnswer;
import com.example.cs25entity.domain.userQuizAnswer.exception.UserQuizAnswerException;
import com.example.cs25entity.domain.userQuizAnswer.exception.UserQuizAnswerExceptionCode;
import com.example.cs25entity.domain.userQuizAnswer.repository.UserQuizAnswerRepository;
import com.example.cs25service.domain.userQuizAnswer.dto.SelectionRateResponseDto;
import com.example.cs25service.domain.userQuizAnswer.dto.UserQuizAnswerRequestDto;
import com.example.cs25service.domain.userQuizAnswer.dto.UserQuizAnswerResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

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
        ReflectionTestUtils.setField(subscription, "serialId", "uuid_subscription");

        // 객관식 퀴즈
        choiceQuiz = Quiz.builder()
                .type(QuizFormatType.MULTIPLE_CHOICE)
                .question("Java is?")
                .answer("1. Programming")
                .commentary("Java is a language.")
                .choice("1. Programming/2. Coffee/3. iceCream/4. latte")
                .category(category)
                .level(QuizLevel.EASY)
                .build();
        ReflectionTestUtils.setField(choiceQuiz, "id", 1L);
        ReflectionTestUtils.setField(choiceQuiz, "serialId", "uuid_quiz");


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
        ReflectionTestUtils.setField(shortAnswerQuiz, "serialId", "uuid_quiz_1");

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

        String subscriptionSerialId = "uuid_subscription";
        String quizSerialId = "uuid_quiz";

        when(subscriptionRepository.findBySerialIdOrElseThrow(subscriptionSerialId)).thenReturn(subscription);
        when(quizRepository.findBySerialIdOrElseThrow(quizSerialId)).thenReturn(choiceQuiz);
        when(userQuizAnswerRepository.existsByQuizIdAndSubscriptionId(choiceQuiz.getId(), subscription.getId())).thenReturn(false);
        when(userQuizAnswerRepository.save(any())).thenReturn(userQuizAnswer);

        // when
        UserQuizAnswerResponseDto userQuizAnswerResponseDto = userQuizAnswerService.submitAnswer(choiceQuiz.getSerialId(), requestDto);

        // then
        assertThat(userQuizAnswer.getId()).isEqualTo(userQuizAnswerResponseDto.getUserQuizAnswerId());
        assertThat(userQuizAnswer.getUserAnswer()).isEqualTo(userQuizAnswerResponseDto.getUserAnswer());
        assertThat(userQuizAnswer.getAiFeedback()).isEqualTo(userQuizAnswerResponseDto.getAiFeedback());
    }

    @Test
    void submitAnswer_구독없음_예외() {
        // given
        String subscriptionSerialId = "uuid_subscription";

        when(subscriptionRepository.findBySerialIdOrElseThrow(subscriptionSerialId))
                .thenThrow(new SubscriptionException(SubscriptionExceptionCode.NOT_FOUND_SUBSCRIPTION_ERROR));

        // when & then
        assertThatThrownBy(() -> userQuizAnswerService.submitAnswer(choiceQuiz.getSerialId(), requestDto))
                .isInstanceOf(SubscriptionException.class)
                .hasMessageContaining("구독 정보를 불러올 수 없습니다.");
    }

    @Test
    void submitAnswer_구독_비활성_예외(){
        //given
        String subscriptionSerialId = "uuid_subscription";

        Subscription subscription = mock(Subscription.class);
        when(subscriptionRepository.findBySerialIdOrElseThrow(subscriptionSerialId)).thenReturn(subscription);
        when(subscription.isActive()).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> userQuizAnswerService.submitAnswer(choiceQuiz.getSerialId(), requestDto))
                .isInstanceOf(SubscriptionException.class)
                .hasMessageContaining("비활성화된 구독자 입니다.");
    }

    @Test
    void submitAnswer_중복답변_예외(){
        //give
        String subscriptionSerialId = "uuid_subscription";
        String quizSerialId = "uuid_quiz";

        when(subscriptionRepository.findBySerialIdOrElseThrow(subscriptionSerialId)).thenReturn(subscription);
        when(quizRepository.findBySerialIdOrElseThrow(quizSerialId)).thenReturn(choiceQuiz);
        when(userQuizAnswerRepository.existsByQuizIdAndSubscriptionId(choiceQuiz.getId(), subscription.getId())).thenReturn(true);
        when(userQuizAnswerRepository.findUserQuizAnswerBySerialIds(quizSerialId, subscriptionSerialId))
                .thenThrow(new UserQuizAnswerException(UserQuizAnswerExceptionCode.NOT_FOUND_ANSWER));

        //when & then
        assertThatThrownBy(() -> userQuizAnswerService.submitAnswer(choiceQuiz.getSerialId(), requestDto))
                .isInstanceOf(UserQuizAnswerException.class)
                .hasMessageContaining("해당 답변을 찾을 수 없습니다");
    }

    @Test
    void submitAnswer_퀴즈없음_예외() {
        // given
        String subscriptionSerialId = "uuid_subscription";
        String quizSerialId = "uuid_quiz";

        when(subscriptionRepository.findBySerialIdOrElseThrow(subscriptionSerialId)).thenReturn(subscription);
        when(quizRepository.findBySerialIdOrElseThrow(quizSerialId))
                .thenThrow(new QuizException(QuizExceptionCode.NOT_FOUND_ERROR));

        // when & then
        assertThatThrownBy(() -> userQuizAnswerService.submitAnswer(choiceQuiz.getSerialId(), requestDto))
                .isInstanceOf(QuizException.class)
                .hasMessageContaining("해당 퀴즈를 찾을 수 없습니다");
    }

    @Test
    void evaluateAnswer_비회원_객관식_정답(){
        //given
        UserQuizAnswer choiceAnswer = UserQuizAnswer.builder()
                .userAnswer("1. Programming")
                .quiz(choiceQuiz)
                .subscription(subscription)
                .build();

        when(userQuizAnswerRepository.findWithQuizAndUserByIdOrElseThrow(choiceAnswer.getId())).thenReturn(choiceAnswer);

        //when
        UserQuizAnswerResponseDto userQuizAnswerResponseDto = userQuizAnswerService.evaluateAnswer(choiceAnswer.getId());

        //then
        assertThat(userQuizAnswerResponseDto.isCorrect()).isTrue();
    }

    @Test
    void evaluateAnswer_비회원_주관식_정답(){
        //given
        UserQuizAnswer shortAnswer = UserQuizAnswer.builder()
                .subscription(subscription)
                .userAnswer("java")
                .quiz(shortAnswerQuiz)
                .build();

        when(userQuizAnswerRepository.findWithQuizAndUserByIdOrElseThrow(shortAnswer.getId())).thenReturn(shortAnswer);

        //when
        UserQuizAnswerResponseDto userQuizAnswerResponseDto = userQuizAnswerService.evaluateAnswer(shortAnswer.getId());

        //then
        assertThat(userQuizAnswerResponseDto.isCorrect()).isTrue();
    }

    @Test
    void evaluateAnswer_회원_객관식_정답_점수부여(){
        //given
        UserQuizAnswer choiceAnswer = UserQuizAnswer.builder()
                .userAnswer("1. Programming")
                .quiz(choiceQuiz)
                .user(user)
                .subscription(subscription)
                .build();

        when(userQuizAnswerRepository.findWithQuizAndUserByIdOrElseThrow(choiceAnswer.getId())).thenReturn(choiceAnswer);

        //when
        UserQuizAnswerResponseDto userQuizAnswerResponseDto = userQuizAnswerService.evaluateAnswer(choiceAnswer.getId());

        //then
        assertThat(userQuizAnswerResponseDto.isCorrect()).isTrue();
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

        when(userQuizAnswerRepository.findWithQuizAndUserByIdOrElseThrow(shortAnswer.getId())).thenReturn(shortAnswer);

        //when
        UserQuizAnswerResponseDto checkSimpleAnswerResponseDto = userQuizAnswerService.evaluateAnswer(shortAnswer.getId());

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

        when(userQuizAnswerRepository.findWithQuizAndUserByIdOrElseThrow(shortAnswer.getId())).thenReturn(shortAnswer);

        //when
        UserQuizAnswerResponseDto userQuizAnswerResponseDto = userQuizAnswerService.evaluateAnswer(shortAnswer.getId());

        //then
        assertThat(userQuizAnswerResponseDto.isCorrect()).isFalse();
    }


    @Test
    void calculateSelectionRateByOption_조회_성공(){
        //given
        String quizSerialId = "uuid_quiz";

        List<UserAnswerDto> answers = List.of(
                new UserAnswerDto("1. Programming"),
                new UserAnswerDto("1. Programming"),
                new UserAnswerDto("2. Coffee"),
                new UserAnswerDto("2. Coffee"),
                new UserAnswerDto("2. Coffee"),
                new UserAnswerDto("3. iceCream"),
                new UserAnswerDto("3. iceCream"),
                new UserAnswerDto("3. iceCream"),
                new UserAnswerDto("4. latte"),
                new UserAnswerDto("4. latte")
        );

        when(quizRepository.findBySerialIdOrElseThrow(quizSerialId)).thenReturn(choiceQuiz);
        when(userQuizAnswerRepository.findUserAnswerByQuizId(choiceQuiz.getId())).thenReturn(answers);

        //when
        SelectionRateResponseDto selectionRateByOption = userQuizAnswerService.calculateSelectionRateByOption(choiceQuiz.getSerialId());

        //then
        assertThat(selectionRateByOption.getTotalCount()).isEqualTo(10);
        Map<String, Double> selectionRates = Map.of(
                "1. Programming", 0.2,
                "2. Coffee", 0.3,
                "3. iceCream", 0.3,
                "4. latte", 0.2
        );
        assertThat(selectionRateByOption.getSelectionRates()).isEqualTo(selectionRates);
    }
}