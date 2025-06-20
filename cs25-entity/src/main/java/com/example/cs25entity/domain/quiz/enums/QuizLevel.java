package com.example.cs25entity.domain.quiz.enums;

public enum QuizLevel {
    EASY(3),
    NORMAL(5),
    HARD(10);

    private final int exp;

    QuizLevel(int exp) {
        this.exp = exp;
    }

    public int getExp() {
        return exp;
    }
}
