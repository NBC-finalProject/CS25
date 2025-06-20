package com.example.cs25service.domain.userQuizAnswer.dto;

import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SelectionRateResponseDto {

    private final Map<String, Double> selectionRates;
    private final long totalCount;
}
