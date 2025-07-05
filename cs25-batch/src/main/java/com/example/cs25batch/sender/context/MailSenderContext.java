package com.example.cs25batch.sender.context;

import com.example.cs25batch.batch.dto.MailDto;
import com.example.cs25batch.config.RateLimiterConfig;
import com.example.cs25batch.sender.MailSenderStrategy;
import io.github.bucket4j.Bucket;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MailSenderContext {
    private final Map<String, MailSenderStrategy> strategyMap;
    private final Map<String, Bucket> bucketMap = new ConcurrentHashMap<>();

    public void send(MailDto dto, String strategyKey) {
        MailSenderStrategy strategy = getStrategy(strategyKey);

        RateLimiterConfig limiterConfig = strategy.getPolicy().rateLimiterConfig();

        strategy.sendQuizMail(dto);
    }

    private Bucket createBucket(RateLimiterConfig config) {
        return Bucket.builder()
            .addLimit(limit -> limit
                .capacity(config.capacity())
                .refillIntervally(config.refill(), Duration.ofMillis(config.millis()))
            )
            .build();
    }

    private MailSenderStrategy getStrategy(String key) {
        MailSenderStrategy strategy = strategyMap.get(key);
        if (strategy == null) {
            throw new IllegalArgumentException("메일 전략이 존재하지 않습니다: " + key);
        }
        return strategy;
    }
}
