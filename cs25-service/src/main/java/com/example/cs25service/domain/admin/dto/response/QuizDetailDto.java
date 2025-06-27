package com.example.cs25service.domain.admin.dto.response;

import com.example.cs25entity.domain.quiz.entity.Quiz;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class QuizDetailDto {

    private final Long quizId;

    private final String question;

    private final String answer;

    private final String commentary;

    private final String choice;

    private final String type;

    private final String category;

    private final LocalDateTime createdAt;

    private final LocalDateTime updatedAt;

    private final Long solvedCnt;

    @Builder
    public QuizDetailDto(Long quizId, String question, String answer, String commentary,
        String choice, String type, String category, LocalDateTime createdAt,
        LocalDateTime updatedAt,
        long solvedCnt) {
        this.quizId = quizId;
        this.question = question;
        this.answer = answer;
        this.commentary = commentary;
        this.choice = choice;
        this.type = type;
        this.category = category;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.solvedCnt = solvedCnt;
    }

    public QuizDetailDto(Quiz quiz, long solvedCnt) {
        this.quizId = quiz.getId();
        this.question = quiz.getQuestion();
        this.answer = quiz.getAnswer();
        this.commentary = quiz.getCommentary();
        this.choice = quiz.getChoice();
        this.type = quiz.getType().name();
        this.createdAt = quiz.getCreatedAt();
        this.updatedAt = quiz.getUpdatedAt();
        this.category = quiz.getCategory().getCategoryType();
        this.solvedCnt = solvedCnt;
    }
}
