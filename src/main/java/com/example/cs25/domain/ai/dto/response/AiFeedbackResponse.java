package com.example.cs25.domain.ai.dto.response;

import lombok.Getter;

@Getter
public class AiFeedbackResponse {
    private Long quizId;
    private boolean isCorrect;
    private String aiFeedback;
    private Long quizAnswerId;

    public AiFeedbackResponse(Long quizId, Boolean isCorrect, String aiFeedback, Long quizAnswerId) {
        this.quizId = quizId;
        this.isCorrect = isCorrect;
        this.aiFeedback = aiFeedback;
        this.quizAnswerId = quizAnswerId;
    }
}
