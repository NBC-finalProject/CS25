package com.example.cs25.domain.quiz.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash("quizAccuracy")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class QuizAccuracy {

    @Id
    private String id; // 예: "quiz:123:category:45"

    private Long quizId;
    private Long categoryId;
    private double accuracy;
}
