package com.example.cs25service.domain.verification.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

import com.example.cs25entity.domain.quiz.entity.QuizCategory;
import com.example.cs25entity.domain.subscription.entity.DayOfWeek;
import com.example.cs25entity.domain.subscription.entity.Subscription;
import com.example.cs25entity.domain.subscription.exception.SubscriptionException;
import com.example.cs25entity.domain.subscription.repository.SubscriptionRepository;
import com.example.cs25entity.domain.user.entity.Role;
import com.example.cs25entity.domain.user.entity.SocialType;
import com.example.cs25entity.domain.user.entity.User;
import com.example.cs25entity.domain.user.exception.UserException;
import com.example.cs25entity.domain.user.repository.UserRepository;
import com.example.cs25service.domain.security.dto.AuthUser;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VerificationPreprocessingServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private VerificationPreprocessingService emailValidationService;

    @Nested
    @DisplayName("isValidEmailCheck 메서드는")
    class IsValidEmailCheck {

        AuthUser authUser = null;

        @Test
        @DisplayName("중복이 없고 형식이 올바른 이메일일 때 예외를 던지지 않는다")
        void validEmail_noDuplication_success() {
            // given
            String email = "test@example.com";

            given(subscriptionRepository.existsByEmail(email)).willReturn(false);

            // when & then
            assertDoesNotThrow(() -> emailValidationService.isValidEmailCheck(email, authUser));
        }

        @Test
        @DisplayName("구독 테이블에 이메일이 이미 존재하면 SubscriptionException을 던진다")
        void duplicateEmailInSubscription_throwsSubscriptionException() {
            // given
            String email = "duplicate@cs25.co.kr";
            given(subscriptionRepository.existsByEmail(email)).willReturn(true);

            // when & then
            assertThrows(SubscriptionException.class,
                () -> emailValidationService.isValidEmailCheck(email, authUser));
        }

        @Test
        @DisplayName("구독을 이미 하고 있는 사용자는 새로운 구독을 만들 수 없다.")
        void duplicateSubscription_throwsUserException() {
            // given
            authUser = new AuthUser("name", "serial-user-001", Role.USER);
            String email = "test@example.com";

            Subscription subscription = Subscription.builder()
                .email("test@example.com")
                .category(QuizCategory.builder()
                    .categoryType("BACKEND")
                    .build())
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(1))
                .subscriptionType(EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY))
                .build();

            User user = User.builder()
                .name(authUser.getName())
                .email(email)
                .socialType(SocialType.KAKAO)
                .subscription(subscription)
                .build();

            given(subscriptionRepository.existsByEmail(email)).willReturn(false);
            given(userRepository.findBySerialId(authUser.getSerialId())).willReturn(
                Optional.ofNullable(user));

            // when & then
            assertThrows(UserException.class,
                () -> emailValidationService.isValidEmailCheck(email, authUser));
        }
    }
}