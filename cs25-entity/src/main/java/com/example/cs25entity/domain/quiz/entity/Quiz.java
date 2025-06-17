package com.example.cs25entity.domain.quiz.entity;

import com.example.cs25common.global.entity.BaseEntity;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Quiz extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private QuizFormatType type;

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

    @Builder
    public Quiz(QuizFormatType type, String question, String answer, String commentary,
        String choice, QuizCategory category) {
        this.type = type;
        this.question = question;
        this.choice = choice;
        this.answer = answer;
        this.commentary = commentary;
        this.category = category;
    }
}