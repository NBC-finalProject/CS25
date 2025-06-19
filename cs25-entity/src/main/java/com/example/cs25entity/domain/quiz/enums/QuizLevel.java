package com.example.cs25entity.domain.quiz.enums;

public enum QuizLevel {
    EASY(30),
    NORMAL(50),
    HARD(100);

    private final int exp;

    QuizLevel(int exp) {
        this.exp = exp;
    }

    public int getExp() {
        return exp;
    }
}
