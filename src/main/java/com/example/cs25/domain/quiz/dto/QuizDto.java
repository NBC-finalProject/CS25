package com.example.cs25.domain.quiz.dto;

import com.example.cs25.domain.quiz.entity.QuizFormatType;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor
public class QuizDto {

    private final Long id;
    private final String quizCategory;
    private final String question;
    private final String choice;
    private final QuizFormatType type;
}
