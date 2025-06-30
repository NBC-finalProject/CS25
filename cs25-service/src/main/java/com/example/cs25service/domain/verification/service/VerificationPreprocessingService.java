package com.example.cs25service.domain.verification.service;

import com.example.cs25entity.domain.subscription.exception.SubscriptionException;
import com.example.cs25entity.domain.subscription.exception.SubscriptionExceptionCode;
import com.example.cs25entity.domain.subscription.repository.SubscriptionRepository;
import com.example.cs25entity.domain.user.exception.UserException;
import com.example.cs25entity.domain.user.exception.UserExceptionCode;
import com.example.cs25entity.domain.user.repository.UserRepository;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VerificationPreprocessingService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    public void isValidEmailCheck(
        @NotBlank(message = "이메일은 필수입니다.") @Email(message = "이메일 형식이 올바르지 않습니다.") String email) {

        if (subscriptionRepository.existsByEmail(email)) {
            throw new SubscriptionException(
                SubscriptionExceptionCode.DUPLICATE_SUBSCRIPTION_EMAIL_ERROR);
        }

        if (userRepository.existsByEmail(email)) {
            throw new UserException(UserExceptionCode.EMAIL_DUPLICATION);
        }
    }
}
