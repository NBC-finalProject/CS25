package com.example.cs25service.domain.subscription.dto;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SubscriptionResponseDto {
    private final Long id;
    private final String category;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final int subscriptionType; // "월화수목금토일" => "1111111"
}
