package com.example.cs25service.domain.profile.dto;

import lombok.Getter;

@Getter
public class WrongQuizResponseDto {

    private final String question;
    private final String userAnswer;
    private final String answer;
    private final String commentary;

    public WrongQuizResponseDto(String question, String userAnswer, String answer, String commentary) {
        this.question = question;
        this.userAnswer = userAnswer;
        this.answer = answer;
        this.commentary = commentary;
    }
}
