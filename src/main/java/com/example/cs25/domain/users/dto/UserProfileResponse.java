package com.example.cs25.domain.users.dto;

import com.example.cs25.domain.subscription.dto.SubscriptionInfoDto;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@Builder
@RequiredArgsConstructor
public class UserProfileResponse {

    private final Long userId;
    private final String name;
    private final String email;

    private final SubscriptionInfoDto subscriptionInfoDto;
}
