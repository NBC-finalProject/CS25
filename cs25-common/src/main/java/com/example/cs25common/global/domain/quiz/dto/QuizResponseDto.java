package com.example.cs25common.global.domain.quiz.dto;

import lombok.Getter;

@Getter
public class QuizResponseDto {

    private final String question;
    private final String answer;
    private final String commentary;

    public QuizResponseDto(String question, String answer, String commentary) {
        this.question = question;
        this.answer = answer;
        this.commentary = commentary;
    }
}
