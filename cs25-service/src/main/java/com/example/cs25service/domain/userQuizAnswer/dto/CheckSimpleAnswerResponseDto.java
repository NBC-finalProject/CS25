package com.example.cs25service.domain.userQuizAnswer.dto;

import lombok.Getter;

@Getter
public class CheckSimpleAnswerResponseDto {
    private final String question;
    private final String userAnswer;
    private final String answer;
    private final String commentary;
    private final boolean isCorrect;

    public CheckSimpleAnswerResponseDto(String question, String userAnswer, String answer, String commentary, boolean isCorrect) {
        this.question = question;
        this.userAnswer = userAnswer;
        this.answer = answer;
        this.commentary = commentary;
        this.isCorrect = isCorrect;
    }
}
