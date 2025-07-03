package com.example.cs25service.domain.admin.dto.request;

import com.example.cs25entity.domain.quiz.enums.QuizFormatType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class QuizUpdateRequestDto {

    private String question;

    private String category;

    private String choice;

    private String answer;

    private String commentary;

    private QuizFormatType quizType;

    public QuizUpdateRequestDto(String question, String category, String choice, String answer,
        String commentary, QuizFormatType quizType) {
        this.question = question;
        this.category = category;
        this.choice = choice;
        this.answer = answer;
        this.commentary = commentary;
        this.quizType = quizType;
    }
}
