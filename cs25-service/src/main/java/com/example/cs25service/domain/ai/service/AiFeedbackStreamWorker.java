package com.example.cs25service.domain.ai.service;

import com.example.cs25service.domain.ai.config.RedisStreamConfig;
import com.example.cs25service.domain.ai.queue.EmitterRegistry;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private final String GROUP_NAME = RedisStreamConfig.GROUP_NAME;
    private final int CORE_WORKER = 2; // 기본 워커
    private final int MAX_WOKRER = 16; // 최대 워커
    private final int SCALING_CHECK_INTERVAL = 5; // 5초마다 큐 상태 확인

    private final AiFeedbackStreamProcessor processor;
    private final RedisTemplate<String, Object> redisTemplate;
    private final EmitterRegistry emitterRegistry;

    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(
        CORE_WORKER,
        MAX_WOKRER,
        60, TimeUnit.SECONDS,
        new LinkedBlockingQueue<>()
    );
    private final AtomicBoolean running = new AtomicBoolean(true);

    @PostConstruct
    public void start() {
        for (int i = 0; i < CORE_WORKER; i++) {
            final String consumerName = "consumer-" + i;
            executor.submit(()-> poll(consumerName));
        }
        executor.submit(this::autoScaleWorkers);
    }

    private void autoScaleWorkers() {
        while (running.get()) {
            try {
                Long queueSize = redisTemplate.opsForStream()
                    .size(RedisStreamConfig.STREAM_KEY);
                int currentThreads = executor.getActiveCount();
                int newThreads = CORE_WORKER;

                // 큐 크기에 따라 확장 기준 설정
                if (queueSize > 100) newThreads = 4;
                if (queueSize > 500) newThreads = 8;
                if (queueSize > 1000) newThreads = 16;

                // 현재 스레드 수보다 늘려야 하면 워커 추가
                if (newThreads > currentThreads) {
                    log.info("워커 수 확장: {}개 -> {}개 (큐 크기: {})", currentThreads, newThreads, queueSize);
                    for (int i = currentThreads; i < newThreads; i++) {
                        final String consumerName = "consumer-" + i;
                        executor.submit(() -> poll(consumerName));
                    }
                }

                // 정해진 시간마다 큐 상태 체크
                TimeUnit.SECONDS.sleep(SCALING_CHECK_INTERVAL);
            } catch (Exception e) {
                log.error("워커 자동 확장 중 오류 발생", e);
            }
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
                            log.warn("해당 answerId={}에 대한 emitter 없습니다.", answerId);
                            redisTemplate.opsForStream()
                                .acknowledge(RedisStreamConfig.STREAM_KEY, GROUP_NAME, message.getId());
                            continue;
                        }

                        processor.stream(answerId, emitter);

                        emitterRegistry.remove(answerId);
                        redisTemplate.opsForSet()
                            .remove(AiFeedbackQueueService.DEDUPLICATION_SET_KEY, answerId);

                        redisTemplate.opsForStream()
                            .acknowledge(RedisStreamConfig.STREAM_KEY, GROUP_NAME, message.getId());
                    }
                }
            } catch (Exception e) {
                log.error("Redis Stream consumer {}에서 오류 발생", consumerName, e);
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