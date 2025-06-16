package com.example.cs25batch.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisConsumerGroupInitalizer implements InitializingBean {

    private final StringRedisTemplate redisTemplate;

    private static final String STREAM = "quiz-email-stream";
    private static final String GROUP = "mail-consumer-group";

    @Override
    public void afterPropertiesSet() {
        try {
            redisTemplate.opsForStream().createGroup(STREAM, ReadOffset.latest(), GROUP);
        } catch (RedisSystemException e) {
            System.out.println("Redis Consumer Group 이미 존재: " + GROUP);
        }
    }
}
