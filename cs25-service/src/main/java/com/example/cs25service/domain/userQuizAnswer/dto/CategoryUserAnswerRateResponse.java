package com.example.cs25service.domain.userQuizAnswer.dto;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CategoryUserAnswerRateResponse {
    private final Map<String, Double> correctRates;

    @Builder
    public CategoryUserAnswerRateResponse(Map<String, Double> correctRates) {
        this.correctRates = correctRates;
    }
}
