package com.example.cs25service.domain.quiz.dto;

import com.example.cs25entity.domain.quiz.entity.QuizCategory;
import com.example.cs25entity.domain.quiz.enums.QuizLevel;
import jakarta.validation.constraints.NotBlank;

public record CreateQuizDto(
    @NotBlank String question,
    @NotBlank String choice,
    @NotBlank String answer,
    String commentary,
    @NotBlank QuizCategory category,
    @NotBlank QuizLevel level
) {
}