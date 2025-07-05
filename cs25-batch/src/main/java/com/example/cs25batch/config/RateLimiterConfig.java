package com.example.cs25batch.config;

public record RateLimiterConfig(
    long capacity, //토큰 용량
    long refill, //몇 개를 채울건지
    long millis //몇 초마다 채울건지
) {
}
