package com.example.cs25service.domain.profile.dto;

import com.example.cs25service.domain.subscription.dto.SubscriptionHistoryDto;
import com.example.cs25service.domain.subscription.dto.SubscriptionInfoDto;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Builder
@RequiredArgsConstructor
@Getter
public class UserSubscriptionResponseDto {

    private final Long userId;
    private final String name;
    private final String email;

    private final List<SubscriptionHistoryDto> subscriptionLogPage;
    private final SubscriptionInfoDto subscriptionInfoDto;
}
