package com.example.cs25entity.domain.quiz.dto;

import com.example.cs25entity.domain.quiz.enums.QuizLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class QuizSearchDto {
    private Long categoryId;
    private QuizLevel level;

    @Builder
    public QuizSearchDto(Long categoryId, QuizLevel level) {
        this.categoryId = categoryId;
        this.level = level;
    }
}
