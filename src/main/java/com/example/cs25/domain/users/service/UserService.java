package com.example.cs25.domain.users.service;

import com.example.cs25.domain.subscription.dto.SubscriptionInfoDto;
import com.example.cs25.domain.subscription.service.SubscriptionService;
import com.example.cs25.domain.users.dto.UserProfileResponse;
import com.example.cs25.domain.users.entity.User;
import com.example.cs25.domain.users.exception.UserException;
import com.example.cs25.domain.users.exception.UserExceptionCode;
import com.example.cs25.domain.users.repository.UserRepository;
import com.example.cs25.global.dto.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;

    public UserProfileResponse getUserProfile(AuthUser authUser) {

        User user = userRepository.findById(authUser.getId())
            .orElseThrow(() ->
                new UserException(UserExceptionCode.NOT_FOUND_USER));

        SubscriptionInfoDto subscriptionInfo = subscriptionService.getSubscription(
            user.getSubscription().getId());

        return UserProfileResponse.builder()
            .userId(user.getId())
            .email(user.getEmail())
            .name(user.getName())
            .subscriptionInfoDto(subscriptionInfo)
            .build();
    }

    public void disableUser(AuthUser authUser) {
        User user = userRepository.findById(authUser.getId())
            .orElseThrow(() ->
                new UserException(UserExceptionCode.NOT_FOUND_USER));

        user.updateDisableUser();
        subscriptionService.disableSubscription(user.getSubscription().getId());
    }
}
