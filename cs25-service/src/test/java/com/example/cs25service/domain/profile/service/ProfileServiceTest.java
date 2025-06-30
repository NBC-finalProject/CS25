package com.example.cs25service.domain.profile.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.example.cs25entity.domain.quiz.entity.Quiz;
import com.example.cs25entity.domain.quiz.entity.QuizCategory;
import com.example.cs25entity.domain.quiz.enums.QuizFormatType;
import com.example.cs25entity.domain.quiz.enums.QuizLevel;
import com.example.cs25entity.domain.quiz.repository.QuizCategoryRepository;
import com.example.cs25entity.domain.subscription.entity.DayOfWeek;
import com.example.cs25entity.domain.subscription.entity.Subscription;
import com.example.cs25entity.domain.subscription.entity.SubscriptionHistory;
import com.example.cs25entity.domain.subscription.repository.SubscriptionHistoryRepository;
import com.example.cs25entity.domain.user.entity.Role;
import com.example.cs25entity.domain.user.entity.User;
import com.example.cs25entity.domain.user.repository.UserRepository;
import com.example.cs25entity.domain.userQuizAnswer.entity.UserQuizAnswer;
import com.example.cs25entity.domain.userQuizAnswer.repository.UserQuizAnswerRepository;
import com.example.cs25service.domain.profile.dto.ProfileResponseDto;
import com.example.cs25service.domain.profile.dto.ProfileWrongQuizResponseDto;
import com.example.cs25service.domain.profile.dto.UserSubscriptionResponseDto;
import com.example.cs25service.domain.security.dto.AuthUser;
import com.example.cs25service.domain.subscription.dto.SubscriptionInfoDto;
import com.example.cs25service.domain.subscription.service.SubscriptionService;
import com.example.cs25service.domain.userQuizAnswer.dto.CategoryUserAnswerRateResponse;
import com.example.cs25service.domain.userQuizAnswer.dto.UserQuizAnswerRequestDto;
import java.time.LocalDate;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private UserQuizAnswerRepository userQuizAnswerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private SubscriptionHistoryRepository subscriptionHistoryRepository;

    @Mock
    private QuizCategoryRepository quizCategoryRepository;

    @InjectMocks
    private ProfileService profileService;

    private Subscription subscription;
    private Subscription subscription1;
    private Quiz quiz;
    private Quiz quiz1;
    private UserQuizAnswer userQuizAnswer;
    private AuthUser authUser;
    private User user;
    private UserQuizAnswerRequestDto requestDto;
    private final Long quizId = 1L;
    private final String serialId = "uuid";
    private List<SubscriptionHistory> subLogs;

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

        subscription1 = Subscription.builder()
            .category(category)
            .email("test@naver.com")
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusMonths(1))
            .subscriptionType(EnumSet.of(DayOfWeek.SUNDAY, DayOfWeek.MONDAY))
            .build();
        ReflectionTestUtils.setField(subscription1, "id", 2L);
        ReflectionTestUtils.setField(subscription, "serialId", "sub-uuid-1");

        quiz = Quiz.builder()
            .type(QuizFormatType.MULTIPLE_CHOICE)
            .question("Java is?")
            .answer("1. Programming Language")
            .commentary("Java is a language.")
            .choice("1. Programming // 2. Coffee")
            .category(category)
            .level(QuizLevel.EASY)
            .build();

        quiz1 = Quiz.builder()
            .type(QuizFormatType.MULTIPLE_CHOICE)
            .question("Java is?")
            .answer("1. Programming Language")
            .commentary("Java is a language.")
            .choice("1. Programming // 2. Coffee")
            .category(category)
            .level(QuizLevel.EASY)
            .build();

        authUser = AuthUser.builder()
            .name("test")
            .role(Role.USER)
            .serialId(serialId)
            .build();

        user = User.builder()
            .email("test@naver.com")
            .name(authUser.getName())
            .role(authUser.getRole())
            .subscription(subscription)
            .score(3.0)
            .build();
        ReflectionTestUtils.setField(user, "id", 1L);
        ReflectionTestUtils.setField(user, "serialId", "user-uuid-1");

        subLogs = List.of(
            SubscriptionHistory.builder().category(category).subscription(subscription).build(),
            SubscriptionHistory.builder().category(category).subscription(subscription1).build()
        );
    }

    @Test
    void getUserSubscription_구독_정보_조회() {
        //given
        when(userRepository.findBySerialId(authUser.getSerialId())).thenReturn(Optional.of(user));

        SubscriptionInfoDto subscriptionInfoDto = SubscriptionInfoDto.builder()
            .category(subscription.getCategory().getCategoryType())
            .email(subscription.getEmail())
            .active(true)
            .startDate(subscription.getStartDate())
            .endDate(subscription.getEndDate())
            .build();

        when(subscriptionService.getSubscription(user.getSubscription().getSerialId())).thenReturn(
            subscriptionInfoDto);
        when(subscriptionHistoryRepository.findAllBySubscriptionId(
            user.getSubscription().getId())).thenReturn(subLogs);

        //when
        UserSubscriptionResponseDto userSubscription = profileService.getUserSubscription(authUser);

        //then
        assertThat(userSubscription.getUserId()).isEqualTo(user.getSerialId());
        assertThat(userSubscription.getEmail()).isEqualTo(user.getEmail());
        assertThat(userSubscription.getName()).isEqualTo(user.getName());
        assertThat(userSubscription.getSubscriptionInfoDto()).isEqualTo(subscriptionInfoDto);
        assertThat(userSubscription.getSubscriptionLogPage())
            .hasSize(2)
            .extracting("subscriptionId")
            .containsExactly(subscription.getId(), subscription1.getId());
    }

    @Test
    void getWrongQuiz_틀린_문제_다시보기() {
        //given
        when(userRepository.findBySerialId(authUser.getSerialId())).thenReturn(Optional.of(user));

        List<UserQuizAnswer> userQuizAnswers = List.of(
            new UserQuizAnswer("정답1", null, true, user, quiz, subscription),
            new UserQuizAnswer("정답2", null, false, user, quiz1, subscription)
        );

        Page<UserQuizAnswer> page = new PageImpl<>(userQuizAnswers, PageRequest.of(0, 10),
            userQuizAnswers.size());
        when(userQuizAnswerRepository.findAllByUserId(user.getId(),
            PageRequest.of(0, 10))).thenReturn(page);

        //when
        ProfileWrongQuizResponseDto wrongQuiz = profileService.getWrongQuiz(authUser,
            PageRequest.of(0, 10));

        //then
        assertThat(wrongQuiz.getUserId()).isEqualTo(authUser.getSerialId());
        assertThat(wrongQuiz.getWrongQuizList())
            .hasSize(1)
            .extracting("userAnswer")
            .containsExactly("정답2");
    }

    @Test
    void getProfile_사용자_정보_조회() {
        //given
        when(userRepository.findBySerialId(authUser.getSerialId())).thenReturn(Optional.of(user));
        when(userRepository.findRankByScore(user.getScore())).thenReturn(1);

        //when
        ProfileResponseDto profile = profileService.getProfile(authUser);

        //then
        assertThat(profile.getName()).isEqualTo(user.getName());
        assertThat(profile.getRank()).isEqualTo(1);
        assertThat(profile.getScore()).isEqualTo(user.getScore());
    }


    @Nested
    @DisplayName("getUserQuizAnswerCorrectRate 테스트 ")
    class getUserQuizAnswerCorrectRateTest {

        private final QuizCategory parentCategory = QuizCategory.builder()
            .categoryType("BACKEND")
            .build();

        private User user;
        private List<QuizCategory> childCategories;

        @BeforeEach
        void setUp() {
            user = User.builder()
                .email("test@naver.com")
                .name(authUser.getName())
                .role(authUser.getRole())
                .subscription(subscription)
                .score(3.0)
                .build();

            ReflectionTestUtils.setField(user, "id", 1L);
            ReflectionTestUtils.setField(user, "serialId", "user-uuid-1");

            QuizCategory child1 = QuizCategory.builder().categoryType("SoftwareDesign")
                .parent(parentCategory).build();
            QuizCategory child2 = QuizCategory.builder().categoryType("Database")
                .parent(parentCategory).build();
            ReflectionTestUtils.setField(child1, "id", 1L);
            ReflectionTestUtils.setField(child2, "id", 2L);
            childCategories = List.of(child1, child2);

            ReflectionTestUtils.setField(parentCategory, "children", childCategories);
        }

        @Test
        @DisplayName("성공")
        void getUserQuizAnswerCorrectRate_success() {
            // given
            List<UserQuizAnswer> softwareDesignAnswers = List.of(
                UserQuizAnswer.builder().isCorrect(true).subscription(subscription).quiz(quiz)
                    .user(user).userAnswer("1").build(),
                UserQuizAnswer.builder().isCorrect(true).subscription(subscription).quiz(quiz1)
                    .user(user).userAnswer("2").build()
            );

            List<UserQuizAnswer> databaseAnswers = List.of(
                UserQuizAnswer.builder().isCorrect(true).subscription(subscription).quiz(quiz)
                    .user(user).userAnswer("1").build(),
                UserQuizAnswer.builder().isCorrect(false).subscription(subscription).quiz(quiz1)
                    .user(user).userAnswer("2").build()
            );

            when(userRepository.findBySerialId(authUser.getSerialId())).thenReturn(
                Optional.of(user));
            when(quizCategoryRepository.findQuizCategoryByUserId(user.getId())).thenReturn(
                parentCategory);
            when(userQuizAnswerRepository.findByUserIdAndQuizCategoryId(user.getId(),
                1L)).thenReturn(softwareDesignAnswers);
            when(userQuizAnswerRepository.findByUserIdAndQuizCategoryId(user.getId(),
                2L)).thenReturn(databaseAnswers);

            // when
            CategoryUserAnswerRateResponse result = profileService.getUserQuizAnswerCorrectRate(
                authUser);

            // then
            assertThat(result.getCorrectRates()).containsEntry("SoftwareDesign", 100.0);
            assertThat(result.getCorrectRates()).containsEntry("Database", 50.0);
        }

        @Test
        @DisplayName("성공 - 퀴즈 로그가 없는 경우 0%로 계산")
        void getUserQuizAnswerCorrectRate_noAnswers() {
            // given
            when(userRepository.findBySerialId(authUser.getSerialId())).thenReturn(
                Optional.of(user));
            when(quizCategoryRepository.findQuizCategoryByUserId(user.getId())).thenReturn(
                parentCategory);
            when(userQuizAnswerRepository.findByUserIdAndQuizCategoryId(user.getId(),
                1L)).thenReturn(Collections.emptyList());
            when(userQuizAnswerRepository.findByUserIdAndQuizCategoryId(user.getId(),
                2L)).thenReturn(Collections.emptyList());

            // when
            CategoryUserAnswerRateResponse result = profileService.getUserQuizAnswerCorrectRate(
                authUser);

            // then
            assertThat(result.getCorrectRates()).containsEntry("SoftwareDesign", 0.0);
            assertThat(result.getCorrectRates()).containsEntry("Database", 0.0);
        }
    }

}