package com.example.cs25service.domain.verification.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

import com.example.cs25entity.domain.subscription.exception.SubscriptionException;
import com.example.cs25entity.domain.subscription.repository.SubscriptionRepository;
import com.example.cs25entity.domain.user.exception.UserException;
import com.example.cs25entity.domain.user.repository.UserRepository;
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

        @Test
        @DisplayName("중복이 없고 형식이 올바른 이메일일 때 예외를 던지지 않는다")
        void validEmail_noDuplication_success() {
            // given
            String email = "test@example.com";
            given(subscriptionRepository.existsByEmail(email)).willReturn(false);
            given(userRepository.existsByEmail(email)).willReturn(false);

            // when & then
            assertDoesNotThrow(() -> emailValidationService.isValidEmailCheck(email));
        }

        @Test
        @DisplayName("구독 테이블에 이메일이 이미 존재하면 SubscriptionException을 던진다")
        void duplicateEmailInSubscription_throwsSubscriptionException() {
            // given
            String email = "duplicate@cs25.co.kr";
            given(subscriptionRepository.existsByEmail(email)).willReturn(true);

            // when & then
            assertThrows(SubscriptionException.class,
                () -> emailValidationService.isValidEmailCheck(email));
        }

        @Test
        @DisplayName("회원 테이블에 이메일이 이미 존재하면 UserException을 던진다")
        void duplicateEmailInUser_throwsUserException() {
            // given
            String email = "user@cs25.co.kr";
            given(subscriptionRepository.existsByEmail(email)).willReturn(false);
            given(userRepository.existsByEmail(email)).willReturn(true);

            // when & then
            assertThrows(UserException.class,
                () -> emailValidationService.isValidEmailCheck(email));
        }
    }
}