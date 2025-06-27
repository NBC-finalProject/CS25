package com.example.cs25batch.batch.service;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BatchProducerService {
    private final StringRedisTemplate redisTemplate;

    //producer
    public void enqueueQuizEmail(Long subscriptionId) {
        redisTemplate.opsForStream()
            .add("quiz-email-stream", Map.of("subscriptionId", subscriptionId.toString()));
    }
}
