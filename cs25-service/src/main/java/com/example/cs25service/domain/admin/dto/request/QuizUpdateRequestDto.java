package com.example.cs25service.domain.admin.dto.request;

import com.example.cs25entity.domain.quiz.entity.QuizFormatType;
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
}
