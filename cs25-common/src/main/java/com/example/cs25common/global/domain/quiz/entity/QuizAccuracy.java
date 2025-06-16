package com.example.cs25common.global.domain.quiz.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;


@Getter
@NoArgsConstructor
@RedisHash(value = "quizAccuracy", timeToLive = 86400)
public class QuizAccuracy {

    @Id
    private String id; // 예: "quiz:123:category:45"

    private Long quizId;
    private Long categoryId;
    private double accuracy;

    @Builder
    public QuizAccuracy(String id, Long quizId, Long categoryId, double accuracy) {
        this.id = id;
        this.quizId = quizId;
        this.categoryId = categoryId;
        this.accuracy = accuracy;
    }
}
