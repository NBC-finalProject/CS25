package com.example.cs25.domain.userQuizAnswer.dto;

import lombok.Getter;

import java.util.Map;

@Getter
public class SelectionRateResponseDto {

    private Map<String, Double> selectionRates;
    private long totalCount;

    public SelectionRateResponseDto(Map<String, Double> selectionRates, long totalCount) {
        this.selectionRates = selectionRates;
        this.totalCount = totalCount;
    }
}
