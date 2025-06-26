package com.example.cs25service.domain.ai.config;


import io.lettuce.core.RedisBusyException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
@RequiredArgsConstructor
public class RedisStreamConfig {

    public static final String STREAM_KEY = "ai-feedback-stream";
    public static final String GROUP_NAME = "ai-feedback-group";

    private final RedisTemplate<String, Object> redisTemplate;

    @PostConstruct
    public void init() {
        try {
            redisTemplate.opsForStream()
                .createGroup(STREAM_KEY, ReadOffset.latest(), GROUP_NAME);
        } catch (RedisSystemException e) {
            if (e.getCause() instanceof RedisBusyException) {
                System.out.println("Consumer group already exists. Skipping...");
            } else {
                throw e;
            }
        }
    }
}
