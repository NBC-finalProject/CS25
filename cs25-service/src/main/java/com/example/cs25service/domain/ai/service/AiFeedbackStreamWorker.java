package com.example.cs25service.domain.ai.service;

import com.example.cs25service.domain.ai.config.RedisStreamConfig;
import com.example.cs25service.domain.ai.queue.EmitterRegistry;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiFeedbackStreamWorker {

    private static final String GROUP_NAME = RedisStreamConfig.GROUP_NAME;
    private static final int WORKER_COUNT = 16;

    private final AiFeedbackStreamProcessor processor;
    private final RedisTemplate<String, Object> redisTemplate;
    private final EmitterRegistry emitterRegistry;

    private final ExecutorService executor = Executors.newFixedThreadPool(WORKER_COUNT);
    private final AtomicBoolean running = new AtomicBoolean(true);

    @PostConstruct
    public void start() {
        for (int i = 0; i < WORKER_COUNT; i++) {
            final String consumerName = "consumer-" + i;
            executor.submit(() -> poll(consumerName));
        }
    }

    private void poll(String consumerName) {
        while (running.get()) {
            try {
                List<MapRecord<String, Object, Object>> messages = redisTemplate.opsForStream()
                    .read(Consumer.from(GROUP_NAME, consumerName),
                        StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),
                        StreamOffset.create(RedisStreamConfig.STREAM_KEY, ReadOffset.lastConsumed()));

                if (messages != null) {
                    for (MapRecord<String, Object, Object> message : messages) {
                        Long answerId = Long.valueOf(message.getValue().get("answerId").toString());
                        SseEmitter emitter = emitterRegistry.get(answerId);

                        if (emitter == null) {
                            log.warn("No emitter found for answerId: {}", answerId);
                            redisTemplate.opsForStream().acknowledge(RedisStreamConfig.STREAM_KEY, GROUP_NAME, message.getId());
                            continue;
                        }

                        processor.stream(answerId, emitter);
                        emitterRegistry.remove(answerId);

                        redisTemplate.opsForSet().remove(AiFeedbackQueueService.DEDUPLICATION_SET_KEY, answerId);

                        redisTemplate.opsForStream()
                            .acknowledge(RedisStreamConfig.STREAM_KEY, GROUP_NAME, message.getId());
                    }
                }
            } catch (Exception e) {
                log.error("Redis Stream consumer {} error", consumerName, e);
            }
        }
    }

    @PreDestroy
    public void stop() {
        running.set(false);
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
