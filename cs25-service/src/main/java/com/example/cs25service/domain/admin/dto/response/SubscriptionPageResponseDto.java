package com.example.cs25service.domain.admin.dto.response;

import com.example.cs25entity.domain.subscription.entity.DayOfWeek;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Builder
public class SubscriptionPageResponseDto {

    private final Long id;

    private final String category;

    private final String email;

    private final boolean isActive;

    private final String serialId;

    private final Set<DayOfWeek> subscriptionType;

}
