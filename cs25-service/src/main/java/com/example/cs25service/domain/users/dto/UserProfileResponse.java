package com.example.cs25service.domain.users.dto;

import com.example.cs25common.global.domain.subscription.dto.SubscriptionHistoryDto;
import com.example.cs25common.global.domain.subscription.dto.SubscriptionInfoDto;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Builder
@RequiredArgsConstructor
@Getter
public class UserProfileResponse {

    private final Long userId;
    private final String name;
    private final String email;

    private final List<SubscriptionHistoryDto> subscriptionLogPage;
    private final SubscriptionInfoDto subscriptionInfoDto;
}
