package com.example.cs25service.domain.ai.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor
public class AiFeedbackResponse {
    private final Long quizId;
    private final Long quizAnswerId;
    private final boolean isCorrect;
    private final String aiFeedback;
}
