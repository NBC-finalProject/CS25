package com.example.cs25service.domain.ai.config;


import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class RedisStreamConfig {

    public static final String STREAM_KEY = "ai-feedback-stream";
    public static final String GROUP_NAME = "ai-feedback-group";

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisStreamConfig(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    public void init() {
        try {
            redisTemplate.opsForStream().createGroup(STREAM_KEY, ReadOffset.latest(), GROUP_NAME);
        } catch (RedisSystemException e) {
            if (!e.getMessage().contains("BUSYGROUP")) {
                throw e;
            }
        }
    }
}
