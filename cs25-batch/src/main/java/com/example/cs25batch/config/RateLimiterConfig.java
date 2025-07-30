package com.example.cs25batch.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.local.LocalBucket;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RateLimiterConfig {

    @Value("${mail.ratelimiter.capacity:14}")
    private Long capacity;

    @Value("${mail.ratelimiter.refill:7}")
    private Long refill;

    @Value("${mail.ratelimiter.millis:500}")
    private Long millis;

    @Bean(name = "bucketEmail")
    public Bucket bucket() {
        return Bucket.builder()
            .addLimit(limit ->
                limit
                    .capacity(capacity)
                    .refillIntervally(refill, Duration.ofMillis(millis))
            )
            .build();
    }


}
