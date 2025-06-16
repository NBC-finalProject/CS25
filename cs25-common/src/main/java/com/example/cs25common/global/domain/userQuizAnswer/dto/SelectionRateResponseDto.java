package com.example.cs25common.global.domain.userQuizAnswer.dto;

import java.util.Map;
import lombok.Getter;

@Getter
public class SelectionRateResponseDto {

    private Map<String, Double> selectionRates;
    private long totalCount;

    public SelectionRateResponseDto(Map<String, Double> selectionRates, long totalCount) {
        this.selectionRates = selectionRates;
        this.totalCount = totalCount;
    }
}
