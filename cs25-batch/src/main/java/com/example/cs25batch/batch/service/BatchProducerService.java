package com.example.cs25batch.batch.service;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BatchProducerService {
    private final StringRedisTemplate redisTemplate;

    private static final String QUIZ_EMAIL_STREAM = "quiz-email-stream";
    //producer
    public void enqueueQuizEmail(Long subscriptionId) {
        redisTemplate.opsForStream()
            .add(QUIZ_EMAIL_STREAM, Map.of("subscriptionId", subscriptionId.toString()));
    }
}
