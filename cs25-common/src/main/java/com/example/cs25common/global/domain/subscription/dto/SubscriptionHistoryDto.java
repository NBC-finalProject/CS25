package com.example.cs25common.global.domain.subscription.dto;

import com.example.cs25common.global.domain.subscription.entity.DayOfWeek;
import com.example.cs25common.global.domain.subscription.entity.Subscription;
import com.example.cs25common.global.domain.subscription.entity.SubscriptionHistory;
import java.time.LocalDate;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;

@Getter
public class SubscriptionHistoryDto {

    private final String categoryType;
    private final Long subscriptionId;
    private final Set<DayOfWeek> subscriptionType;
    private final LocalDate startDate;
    private final LocalDate updateDate;

    @Builder
    public SubscriptionHistoryDto(String categoryType, Long subscriptionId,
        Set<DayOfWeek> subscriptionType,
        LocalDate startDate, LocalDate updateDate) {
        this.categoryType = categoryType;
        this.subscriptionId = subscriptionId;
        this.subscriptionType = subscriptionType;
        this.startDate = startDate;
        this.updateDate = updateDate;
    }

    public static SubscriptionHistoryDto fromEntity(SubscriptionHistory log) {
        return SubscriptionHistoryDto.builder()
            .categoryType(log.getCategory().getCategoryType())
            .subscriptionId(log.getSubscription().getId())
            .subscriptionType(Subscription.decodeDays(log.getSubscriptionType()))
            .startDate(log.getStartDate())
            .updateDate(log.getUpdateDate())
            .build();
    }
}
