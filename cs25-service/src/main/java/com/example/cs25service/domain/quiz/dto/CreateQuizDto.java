package com.example.cs25service.domain.quiz.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateQuizDto(
    @NotBlank String question,
    @NotBlank String choice,
    @NotBlank String answer,
    String commentary
) {

}