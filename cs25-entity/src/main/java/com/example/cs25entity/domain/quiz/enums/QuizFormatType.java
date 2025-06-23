package com.example.cs25entity.domain.quiz.enums;

public enum QuizFormatType {
    MULTIPLE_CHOICE(1),   // 객관식
    SHORT_ANSWER(3),    // 단답식
    SUBJECTIVE(5);       // 서술형

    private final int score;

    QuizFormatType(int score) {
        this.score = score;
    }

    public int getScore() {
        return score;
    }
}
