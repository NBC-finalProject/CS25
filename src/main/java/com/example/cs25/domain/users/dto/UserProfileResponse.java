package com.example.cs25.domain.users.dto;

import com.example.cs25.domain.subscription.dto.SubscriptionInfoDto;
import com.example.cs25.domain.subscription.dto.SubscriptionLogDto;
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

    private final List<SubscriptionLogDto> subscriptionLogPage;
    private final SubscriptionInfoDto subscriptionInfoDto;
}
