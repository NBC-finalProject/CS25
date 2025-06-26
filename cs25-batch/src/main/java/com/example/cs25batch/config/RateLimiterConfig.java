package com.example.cs25batch.config;

import com.google.common.util.concurrent.RateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimiterConfig {
    @Bean
    public RateLimiter rateLimiter() {
        // 초당 10건 제한 (100ms 간격)
        return RateLimiter.create(10.0);
    }
}