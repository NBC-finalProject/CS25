package com.example.cs25service.domain.admin.dto;

import com.example.cs25entity.domain.quiz.entity.QuizFormatType;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class QuizCreateRequestDto {

    @NotBlank
    private String question;

    @NotBlank
    private String category;

    private String choice;

    @NotBlank
    private String answer;

    private String commentary;

    @NotBlank
    private QuizFormatType quizType;
}
