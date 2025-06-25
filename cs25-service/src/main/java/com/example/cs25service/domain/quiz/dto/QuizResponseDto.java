package com.example.cs25service.domain.quiz.dto;

import com.example.cs25entity.domain.quiz.entity.Quiz;
import com.example.cs25entity.domain.quiz.enums.QuizLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class QuizResponseDto {

    private final Long id;
    private final String question;
    private final String answer;
    private final String commentary;
    private final String level;

    @Builder
    public QuizResponseDto(Long id, String question, String answer, String commentary, QuizLevel level) {
        this.id = id;
        this.question = question;
        this.answer = answer;
        this.commentary = commentary;
        this.level = level.name();
    }

    public static QuizResponseDto from(Quiz quiz) {
        return QuizResponseDto.builder()
            .id(quiz.getId())
            .question(quiz.getQuestion())
            .answer(quiz.getAnswer())
            .commentary(quiz.getCommentary())
            .level(quiz.getLevel())
            .build();
    }
}
