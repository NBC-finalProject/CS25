package com.example.cs25service.domain.subscription.dto;

import com.example.cs25entity.domain.quiz.entity.QuizCategory;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
public class SubscriptionResponseDto {

    private final Long id;
    private final String category;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final int subscriptionType; // "월화수목금토일" => "1111111"

    public SubscriptionResponseDto(Long id, String category, LocalDate startDate,
        LocalDate endDate, int subscriptionType) {
        this.id = id;
        this.category = category;
        this.startDate = startDate;
        this.endDate = endDate;
        this.subscriptionType = subscriptionType;
    }
}
