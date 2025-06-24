package com.example.cs25service.domain.quiz.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class QuizResponseDto {

    private final Long id;
    private final String question;
    private final String answer;
    private final String commentary;

    @Builder
    public QuizResponseDto(Long id, String question, String answer, String commentary) {
        this.id = id;
        this.question = question;
        this.answer = answer;
        this.commentary = commentary;
    }
}
