package com.example.cs25entity.domain.quiz.enums;

public enum QuizFormatType {
    MULTIPLE_CHOICE(1),   // 객관식
    SUBJECTIVE(3),         // 서술형
    SHORT_ANSWER(5);     // 단답식

    private final int score;

    QuizFormatType(int score) {
        this.score = score;
    }

    public int getScore() {
        return score;
    }
}
