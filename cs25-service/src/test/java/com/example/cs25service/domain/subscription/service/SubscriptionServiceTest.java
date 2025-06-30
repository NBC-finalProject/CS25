package com.example.cs25service.domain.subscription.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;

import com.example.cs25entity.domain.quiz.entity.QuizCategory;
import com.example.cs25entity.domain.quiz.exception.QuizException;
import com.example.cs25entity.domain.quiz.repository.QuizCategoryRepository;
import com.example.cs25entity.domain.subscription.entity.DayOfWeek;
import com.example.cs25entity.domain.subscription.entity.Subscription;
import com.example.cs25entity.domain.subscription.entity.SubscriptionPeriod;
import com.example.cs25entity.domain.subscription.exception.SubscriptionException;
import com.example.cs25entity.domain.subscription.repository.SubscriptionHistoryRepository;
import com.example.cs25entity.domain.subscription.repository.SubscriptionRepository;
import com.example.cs25entity.domain.user.entity.User;
import com.example.cs25entity.domain.user.exception.UserException;
import com.example.cs25entity.domain.user.repository.UserRepository;
import com.example.cs25service.domain.security.dto.AuthUser;
import com.example.cs25service.domain.subscription.dto.SubscriptionInfoDto;
import com.example.cs25service.domain.subscription.dto.SubscriptionRequestDto;
import com.example.cs25service.domain.subscription.dto.SubscriptionResponseDto;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private SubscriptionHistoryRepository subscriptionHistoryRepository;

    @Mock
    private QuizCategoryRepository quizCategoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SubscriptionService subscriptionService;

    private QuizCategory quizCategory;
    private Subscription subscription;
    private User user;
    private AuthUser authUser;
    private SubscriptionRequestDto requestDto;

    @BeforeEach
    void setUp() {
        quizCategory = QuizCategory.builder()
            .categoryType("BACKEND")
            .build();

        subscription = Subscription.builder()
            .email("test@test.com")
            .category(quizCategory)
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusMonths(1))
            .subscriptionType(EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY))
            .build();

        // 리플렉션을 이용하여 ID 값을 1L로 지정
        try {
            Field idField = Subscription.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(subscription, 1L);
        } catch (Exception exception) {
            // 예외처리
        }

        user = User.builder()
            .email("test@test.com")
            .name("testuser")
            .build();

        authUser = AuthUser.builder()
            .name("testuser")
            .serialId("user-serial-id")
            .build();

        requestDto = SubscriptionRequestDto.builder()
            .category("BACKEND")
            .email("test@test.com")
            .period(SubscriptionPeriod.ONE_MONTH)
            .days(EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY))
            .build();
    }

    @Test
    @DisplayName("구독 정보 조회 성공")
    void getSubscription_success() {
        // given
        String subscriptionId = "id";
        given(subscriptionRepository.findBySerialId(subscriptionId))
            .willReturn(Optional.of(subscription));

        // when
        SubscriptionInfoDto result = subscriptionService.getSubscription(subscriptionId);

        // then
        assertEquals("BACKEND", result.getCategory());
        assertEquals("test@test.com", result.getEmail());
        assertTrue(result.isActive());
        assertEquals(subscription.getStartDate(), result.getStartDate());
        assertEquals(subscription.getEndDate(), result.getEndDate());
    }

    @Test
    @DisplayName("존재하지 않는 구독 ID로 조회 시 예외 발생")
    void getSubscription_notFound() {
        // given
        String subscriptionId = "id";
        given(subscriptionRepository.findBySerialId(subscriptionId))
            .willReturn(Optional.empty());

        // when & then
        assertThrows(QuizException.class,
            () -> subscriptionService.getSubscription(subscriptionId));
    }

    @Test
    @DisplayName("로그인 사용자 구독 생성 성공")
    void createSubscription_withAuthUser_success() {
        // given
        given(quizCategoryRepository.findByCategoryTypeOrElseThrow("BACKEND"))
            .willReturn(quizCategory);
        given(userRepository.findBySerialId("user-serial-id"))
            .willReturn(Optional.of(user));
        given(subscriptionRepository.save(any(Subscription.class)))
            .willAnswer(invocation -> {
                Subscription savedSubscription = invocation.getArgument(0);
                try {
                    Field idField = Subscription.class.getDeclaredField("id");
                    idField.setAccessible(true);
                    idField.set(savedSubscription, 1L);
                } catch (Exception e) {
                    // 예외처리
                }
                return savedSubscription;
            });
        given(subscriptionHistoryRepository.save(any()))
            .willReturn(null);

        // when
        SubscriptionResponseDto result = subscriptionService.createSubscription(requestDto,
            authUser);

        // then
        assertNotNull(result);
        assertEquals("BACKEND", result.getCategory());
        assertEquals(subscription.getStartDate(), result.getStartDate());
        assertEquals(subscription.getEndDate(), result.getEndDate());
    }

    @Test
    @DisplayName("비로그인 사용자 구독 생성 성공")
    void createSubscription_withoutAuthUser_success() {
        // given
        given(quizCategoryRepository.findByCategoryTypeOrElseThrow("BACKEND"))
            .willReturn(quizCategory);
        given(subscriptionRepository.existsByEmail("test@test.com"))
            .willReturn(false);
        given(subscriptionRepository.save(any(Subscription.class)))
            .willAnswer(invocation -> {
                Subscription savedSubscription = invocation.getArgument(0);
                try {
                    Field idField = Subscription.class.getDeclaredField("id");
                    idField.setAccessible(true);
                    idField.set(savedSubscription, 1L);
                } catch (Exception e) {
                    // 예외처리
                }
                return savedSubscription;
            });
        given(subscriptionHistoryRepository.save(any()))
            .willReturn(null);

        // when
        SubscriptionResponseDto result = subscriptionService.createSubscription(requestDto, null);

        // then
        assertNotNull(result);
        assertEquals("BACKEND", result.getCategory());
        assertEquals(subscription.getStartDate(), result.getStartDate());
        assertEquals(subscription.getEndDate(), result.getEndDate());
    }

    @Test
    @DisplayName("자식 카테고리로 구독 생성 시 예외 발생")
    void createSubscription_childCategory_exception() {
        // given
        QuizCategory childCategory = QuizCategory.builder()
            .categoryType("BACKEND")
            .parent(quizCategory)
            .build();

        given(quizCategoryRepository.findByCategoryTypeOrElseThrow("BACKEND"))
            .willReturn(childCategory);

        // when & then
        QuizException ex = assertThrows(QuizException.class,
            () -> subscriptionService.createSubscription(requestDto, authUser));
        assertThat(ex.getMessage()).contains("대분류 카테고리가 필요합니다.");
    }

    @Test
    @DisplayName("이미 구독 중인 사용자의 중복 구독 생성 시 예외 발생")
    void createSubscription_duplicateSubscription_exception() {
        // given
        User userWithSubscription = User.builder()
            .email("test@test.com")
            .name("testuser")
            .subscription(subscription)
            .build();

        given(quizCategoryRepository.findByCategoryTypeOrElseThrow("BACKEND"))
            .willReturn(quizCategory);
        given(userRepository.findBySerialId("user-serial-id"))
            .willReturn(Optional.of(userWithSubscription));

        // when & then
        SubscriptionException ex = assertThrows(SubscriptionException.class,
            () -> subscriptionService.createSubscription(requestDto, authUser));
        assertThat(ex.getMessage()).contains("이미 구독중인 이메일입니다.");
    }

    @Test
    @DisplayName("구독 정보 업데이트 성공")
    void updateSubscription_success() {
        // given
        given(subscriptionRepository.findBySerialId("id"))
            .willReturn(Optional.of(subscription));
        given(quizCategoryRepository.findByCategoryTypeOrElseThrow("BACKEND"))
            .willReturn(quizCategory);

        // when
        assertDoesNotThrow(() -> subscriptionService.updateSubscription("id", requestDto));

        // then
        verify(subscriptionHistoryRepository).save(any());
    }

    @Test
    @DisplayName("1년 초과 구독 기간 업데이트 시 예외 발생")
    void updateSubscription_exceedsMaxPeriod_exception() {
        // given
        Subscription overSubscription = Subscription.builder()
            .email("test@test.com")
            .category(quizCategory)
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusMonths(11)) // 이미 11개월
            .subscriptionType(EnumSet.of(DayOfWeek.MONDAY))
            .build();

        SubscriptionRequestDto overRequestDto = SubscriptionRequestDto.builder()
            .category("BACKEND")
            .period(SubscriptionPeriod.THREE_MONTHS) // 3개월 더 추가하면 1년 초과
            .days(EnumSet.of(DayOfWeek.MONDAY))
            .active(true)
            .build();

        given(subscriptionRepository.findBySerialId("id"))
            .willReturn(Optional.of(overSubscription));
        given(quizCategoryRepository.findByCategoryTypeOrElseThrow("BACKEND"))
            .willReturn(quizCategory);

        // when & then
        SubscriptionException ex = assertThrows(SubscriptionException.class,
            () -> subscriptionService.updateSubscription("id", overRequestDto));
        assertThat(ex.getMessage()).contains("구독 시작일로부터 1년 이상 구독할 수 없습니다.");
    }

    @Test
    @DisplayName("구독 취소 성공")
    void cancelSubscription_success() {
        // given
        given(subscriptionRepository.findBySerialId("id"))
            .willReturn(Optional.of(subscription));

        // when
        assertDoesNotThrow(() -> subscriptionService.cancelSubscription("id"));

        // then
        verify(subscriptionHistoryRepository).save(any());
    }

    @Test
    @DisplayName("이메일 중복 체크 - 중복되지 않은 경우")
    void checkEmail_noDuplicate_success() {
        // given
        String email = "123@123.com";
        given(subscriptionRepository.existsByEmail(email))
            .willReturn(false);

        // when & then
        assertDoesNotThrow(() -> subscriptionService.checkEmail(email));
    }

    @Test
    @DisplayName("이메일 중복 체크 - 중복된 경우 예외 발생")
    void checkEmail_duplicate_exception() {
        // given
        String email = "123@123.com";
        given(subscriptionRepository.existsByEmail(email))
            .willReturn(true);

        // when & then
        SubscriptionException ex = assertThrows(SubscriptionException.class,
            () -> subscriptionService.checkEmail(email));
        assertThat(ex.getMessage()).contains("이미 구독중인 이메일입니다.");

    }

    @Test
    @DisplayName("존재하지 않는 사용자로 구독 생성 시 예외 발생")
    void createSubscription_userNotFound_exception() {
        // given
        given(quizCategoryRepository.findByCategoryTypeOrElseThrow("BACKEND"))
            .willReturn(quizCategory);
        given(userRepository.findBySerialId("user-serial-id"))
            .willReturn(Optional.empty());

        // when & then
        UserException ex = assertThrows(UserException.class,
            () -> subscriptionService.createSubscription(requestDto, authUser));
        assertThat(ex.getMessage()).contains("해당 유저를 찾을 수 없습니다.");
    }
}