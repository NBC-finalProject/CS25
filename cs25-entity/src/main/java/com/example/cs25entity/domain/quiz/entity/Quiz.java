package com.example.cs25entity.domain.quiz.entity;

import com.example.cs25common.global.entity.BaseEntity;
import com.example.cs25entity.domain.quiz.enums.QuizFormatType;
import com.example.cs25entity.domain.quiz.enums.QuizLevel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
public class Quiz extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private QuizFormatType type;

    @Column(columnDefinition = "TEXT")
    private String question; // 문제

    @Column(columnDefinition = "TEXT")
    private String answer; // 답변

    @Column(columnDefinition = "TEXT")
    private String commentary; // 해설

    @Column(columnDefinition = "TEXT")
    private String choice; // 객관식 보기 (ex. 1. OOO // 2. OOO // ...)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_category_id")
    private QuizCategory category;

    @Enumerated(EnumType.STRING)
    private QuizLevel level;

    private boolean isDeleted;

    @Column(unique = true)
    private String serialId; //uuid

    @Builder
    public Quiz(QuizFormatType type, String question, String answer, String commentary,
        String choice, QuizCategory category, QuizLevel level) {
        this.type = type;
        this.question = question;
        this.choice = choice;
        this.answer = answer;
        this.commentary = commentary;
        this.category = category;
        this.level = level;
        this.isDeleted = false;
        this.serialId = UUID.randomUUID().toString();
    }

    public void updateCategory(QuizCategory quizCategory) {
        this.category = quizCategory;
    }

    public void updateChoice(String choice) {
        if (this.type == QuizFormatType.MULTIPLE_CHOICE) {
            this.choice = choice;
        } else {
            this.choice = null;
        }
    }

    public void updateQuestion(String question) {
        this.question = question;
    }

    public void updateAnswer(String answer) {
        this.answer = answer;
    }

    public void updateCommentary(String commentary) {
        this.commentary = commentary;
    }

    public void updateType(QuizFormatType type) {
        this.type = type;
        updateChoice(this.choice);
    }

    public void enableQuiz() {
        this.isDeleted = false;
    }

    public void disableQuiz() {
        this.isDeleted = true;
    }
}