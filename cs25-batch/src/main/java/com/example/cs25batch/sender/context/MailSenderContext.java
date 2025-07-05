package com.example.cs25batch.sender.context;

import com.example.cs25batch.batch.dto.MailDto;
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
        Bucket bucket = strategy.getRateLimiter();

        if (!bucket.tryConsume(1)) {
            throw new RuntimeException("전략에 해당하는 RateLimiter가 존재하지 않습니다.: " + strategyKey);
        }

        strategy.sendQuizMail(dto);
    }

    private MailSenderStrategy getStrategy(String key) {
        MailSenderStrategy strategy = strategyMap.get(key);
        if (strategy == null) {
            throw new IllegalArgumentException("메일 전략이 존재하지 않습니다: " + key);
        }
        return strategy;
    }
}
