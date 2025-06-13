package com.example.cs25.global.jwt.service;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final StringRedisTemplate redisTemplate;

    private static final String PREFIX = "RT:";

    public void save(Long userId, String refreshToken, Duration ttl) {
        String key = PREFIX + userId;
        if (ttl == null) {
                throw new IllegalArgumentException("TTL must not be null");
        }
        redisTemplate.opsForValue().set(key, refreshToken, ttl);
    }

    public String get(Long userId) {
        return redisTemplate.opsForValue().get(PREFIX + userId);
    }

    public void delete(Long userId) {
        redisTemplate.delete(PREFIX + userId);
    }

    public boolean exists(Long userId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(PREFIX + userId));
    }
}
