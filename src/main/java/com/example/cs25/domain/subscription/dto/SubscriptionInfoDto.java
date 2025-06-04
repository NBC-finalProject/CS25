package com.example.cs25.domain.subscription.dto;

import com.example.cs25.domain.subscription.entity.DayOfWeek;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SubscriptionInfoDto {

    private String categoryName;
    private Long period;
    private Set<DayOfWeek> subscriptionType;
}
