package com.example.cs25entity.domain.userQuizAnswer.dto;

import lombok.Getter;

@Getter
public class UserAnswerDto {

    private final String userAnswer;

    public UserAnswerDto(String userAnswer) {
        this.userAnswer = userAnswer;
    }
}
