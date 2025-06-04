package com.example.cs25.domain.users.service;

import com.example.cs25.domain.subscription.dto.SubscriptionHistoryDto;
import com.example.cs25.domain.subscription.dto.SubscriptionInfoDto;
import com.example.cs25.domain.subscription.entity.SubscriptionHistory;
import com.example.cs25.domain.subscription.repository.SubscriptionHistoryRepository;
import com.example.cs25.domain.subscription.service.SubscriptionService;
import com.example.cs25.domain.users.dto.UserProfileResponse;
import com.example.cs25.domain.users.entity.User;
import com.example.cs25.domain.users.exception.UserException;
import com.example.cs25.domain.users.exception.UserExceptionCode;
import com.example.cs25.domain.users.repository.UserRepository;
import com.example.cs25.global.dto.AuthUser;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;
    private final SubscriptionHistoryRepository subscriptionHistoryRepository;

    public UserProfileResponse getUserProfile(AuthUser authUser) {

        User user = userRepository.findById(authUser.getId())
            .orElseThrow(() ->
                new UserException(UserExceptionCode.NOT_FOUND_USER));

        Long subscriptionId = user.getSubscription().getId();

        SubscriptionInfoDto subscriptionInfo = subscriptionService.getSubscription(
            subscriptionId);

        //로그 다 모아와서 리스트로 만들기
        List<SubscriptionHistory> subLogs = subscriptionHistoryRepository
            .findAllBySubscriptionId(subscriptionId);
        List<SubscriptionHistoryDto> dtoList = subLogs.stream()
            .map(SubscriptionHistoryDto::fromEntity)
            .toList();

        return UserProfileResponse.builder()
            .userId(user.getId())
            .email(user.getEmail())
            .name(user.getName())
            .subscriptionLogPage(dtoList)
            .subscriptionInfoDto(subscriptionInfo)
            .build();
    }

    @Transactional
    public void disableUser(AuthUser authUser) {
        User user = userRepository.findById(authUser.getId())
            .orElseThrow(() ->
                new UserException(UserExceptionCode.NOT_FOUND_USER));

        user.updateDisableUser();
        subscriptionService.cancelSubscription(user.getSubscription().getId());
    }
}
