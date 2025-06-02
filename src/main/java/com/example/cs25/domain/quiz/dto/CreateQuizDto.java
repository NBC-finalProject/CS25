package com.example.cs25.domain.quiz.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateQuizDto(
    @NotBlank String question,
    @NotBlank String choice,
    @NotBlank String answer,
    String commentary
) {

}