package com.example.cs25.domain.subscription.dto;

import com.example.cs25.domain.subscription.entity.DayOfWeek;
import com.example.cs25.domain.subscription.entity.Subscription;
import com.example.cs25.domain.subscription.entity.SubscriptionLog;
import java.util.Set;
import lombok.Builder;

@Builder
public record SubscriptionLogDto(String categoryType, Long subscriptionId,
                                 Set<DayOfWeek> subscriptionType) {

    public static SubscriptionLogDto fromEntity(SubscriptionLog log) {
        return SubscriptionLogDto.builder()
            .categoryType(log.getCategory().getCategoryType())
            .subscriptionId(log.getSubscription().getId())
            .subscriptionType(Subscription.decodeDays(log.getSubscriptionType()))
            .build();
    }
}
