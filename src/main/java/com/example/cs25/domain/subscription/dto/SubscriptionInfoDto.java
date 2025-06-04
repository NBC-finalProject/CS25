package com.example.cs25.domain.subscription.dto;

import com.example.cs25.domain.subscription.entity.DayOfWeek;
import java.util.Set;
import lombok.Builder;

@Builder
public record SubscriptionInfoDto(String categoryName, Long period,
                                  Set<DayOfWeek> subscriptionType) {

}
