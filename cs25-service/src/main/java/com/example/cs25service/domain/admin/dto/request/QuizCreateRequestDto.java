package com.example.cs25service.domain.admin.dto.request;

import com.example.cs25entity.domain.quiz.enums.QuizFormatType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotNull
    private QuizFormatType quizType;

    public QuizCreateRequestDto(String question, String category, String choice, String answer,
        String commentary, QuizFormatType quizType) {
        this.question = question;
        this.category = category;
        this.choice = choice;
        this.answer = answer;
        this.commentary = commentary;
        this.quizType = quizType;
    }
}
