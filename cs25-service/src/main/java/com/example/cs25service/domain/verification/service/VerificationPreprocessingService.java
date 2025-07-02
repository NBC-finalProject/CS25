package com.example.cs25service.domain.verification.service;

import com.example.cs25entity.domain.subscription.exception.SubscriptionException;
import com.example.cs25entity.domain.subscription.exception.SubscriptionExceptionCode;
import com.example.cs25entity.domain.subscription.repository.SubscriptionRepository;
import com.example.cs25entity.domain.user.entity.User;
import com.example.cs25entity.domain.user.exception.UserException;
import com.example.cs25entity.domain.user.exception.UserExceptionCode;
import com.example.cs25entity.domain.user.repository.UserRepository;
import com.example.cs25service.domain.security.dto.AuthUser;
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
        @NotBlank(message = "이메일은 필수입니다.") @Email(message = "이메일 형식이 올바르지 않습니다.") String email,
        AuthUser authUser) {

        /*
         * 이미 구독정보에 등록된 이메일인지 확인하는 메서드
         * 유저의 경우, 소셜이메일이 아닌 다른 이메일로 구독할 수 있기 때문에
         * 따로 유저 이메일 중복 예외처리를 하지 않음
         */
        if (subscriptionRepository.existsByEmail(email)) {
            throw new SubscriptionException(
                SubscriptionExceptionCode.DUPLICATE_SUBSCRIPTION_EMAIL_ERROR);
        }

        if (authUser != null) {
            User user = userRepository.findBySerialIdOrElseThrow(authUser.getSerialId());

            if (user.getSubscription() != null) {
                throw new UserException(
                    UserExceptionCode.DUPLICATE_SUBSCRIPTION_ERROR);
            }
        }

    }
}
