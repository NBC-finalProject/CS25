package com.example.cs25service.domain.ai.service;

import com.example.cs25service.domain.ai.config.RedisStreamConfig;
import com.example.cs25service.domain.ai.queue.EmitterRegistry;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.util.List;
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

    private static final String GROUP_NAME = RedisStreamConfig.GROUP_NAME;
    private static final int CORE_WORKER = 2;             // 기본 워커
    private static final int MAX_WORKER = 16;             // 최대 워커
    private static final int SCALING_CHECK_INTERVAL = 5;  // 워커 상태 체크 주기 (초)

    private final AiFeedbackStreamProcessor processor;
    private final RedisTemplate<String, Object> redisTemplate;
    private final EmitterRegistry emitterRegistry;

    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(
        CORE_WORKER,
        MAX_WORKER,
        60, TimeUnit.SECONDS,
        new LinkedBlockingQueue<>()
    );

    private final AtomicBoolean running = new AtomicBoolean(true);

    @PostConstruct
    public void start() {
        // 초기 워커 실행
        for (int i = 0; i < CORE_WORKER; i++) {
            final String consumerName = "consumer-" + i;
            executor.submit(() -> poll(consumerName));
        }
        // 자동 스케일링 워커 실행
        executor.submit(this::autoScaleWorkers);
    }

    private void autoScaleWorkers() {
        while (running.get()) {
            try {
                long queueSize = redisTemplate.opsForStream().size(RedisStreamConfig.STREAM_KEY);
                int currentThreads = executor.getActiveCount();
                int newThreads = calculateTargetWorkerCount(queueSize);

                if (newThreads > currentThreads) {
                    // 워커 확장
                    log.info("워커 수 확장: {}개 -> {}개 (큐 크기: {})", currentThreads, newThreads, queueSize);
                    for (int i = currentThreads; i < newThreads; i++) {
                        final String consumerName = "consumer-" + i;
                        executor.submit(() -> poll(consumerName));
                    }
                } else if (newThreads < currentThreads) {
                    // 워커 축소
                    int threadsToReduce = currentThreads - newThreads;
                    log.info("워커 수 축소: {}개 -> {}개 (큐 크기: {})", currentThreads, newThreads, queueSize);
                    for (int i = 0; i < threadsToReduce; i++) {
                        executor.remove(() -> {}); // 큐에 대기중인 task 제거 (간단한 축소 처리)
                    }
                }

                TimeUnit.SECONDS.sleep(SCALING_CHECK_INTERVAL);
            } catch (Exception e) {
                log.error("워커 자동 스케일링 중 오류 발생", e);
            }
        }
    }

    /**
     * 큐 크기에 따른 목표 워커 수 계산
     */
    private int calculateTargetWorkerCount(long queueSize) {
        if (queueSize > 1000) return 16;
        else if (queueSize > 500) return 8;
        else if (queueSize > 100) return 4;
        else return CORE_WORKER;
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
                            log.warn("해당 answerId={}에 대한 emitter가 없습니다.", answerId);
                            redisTemplate.opsForStream()
                                .acknowledge(RedisStreamConfig.STREAM_KEY, GROUP_NAME, message.getId());
                            continue;
                        }

                        processor.stream(answerId, emitter);
                        emitterRegistry.remove(answerId);
                        redisTemplate.opsForSet().remove(AiFeedbackQueueService.DEDUPLICATION_SET_KEY, answerId);
                        redisTemplate.opsForStream().acknowledge(RedisStreamConfig.STREAM_KEY, GROUP_NAME, message.getId());
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
