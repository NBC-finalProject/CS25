package com.example.cs25service.domain.subscription.dto;

import com.example.cs25entity.domain.subscription.entity.DayOfWeek;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Builder
public class SubscriptionInfoDto {

    private final String category;

    private final Long period;

    private final Set<DayOfWeek> subscriptionType;
}
