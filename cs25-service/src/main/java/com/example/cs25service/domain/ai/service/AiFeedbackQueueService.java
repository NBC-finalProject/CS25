package com.example.cs25service.domain.ai.service;

import com.example.cs25service.domain.ai.config.RedisStreamConfig;
import com.example.cs25service.domain.ai.queue.EmitterRegistry;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiFeedbackQueueService {

    private final EmitterRegistry emitterRegistry;
    private final RedisTemplate<String, Object> redisTemplate;
    public static final String DEDUPLICATION_SET_KEY = "ai-feedback-dedup-set";

    public void enqueue(Long answerId, SseEmitter emitter) {
        try {
            // 중복 체크 (이미 등록된 경우 enqueue 하지 않음)
            Long added = redisTemplate.opsForSet().add(DEDUPLICATION_SET_KEY, String.valueOf(answerId));
            if (Boolean.FALSE.equals(added)) {
                log.info("Duplicate enqueue prevented for answerId {}", answerId);
                return;
            }

            emitterRegistry.register(answerId, emitter);
            Map<String, Object> data = Map.of("answerId", answerId);
            redisTemplate.opsForStream().add(RedisStreamConfig.STREAM_KEY, data);
        } catch (Exception e) {
            emitterRegistry.remove(answerId);
            redisTemplate.opsForSet().remove(DEDUPLICATION_SET_KEY, answerId); // 실패 시 롤백
            completeWithError(emitter, e);
        }
    }

    private void completeWithError(SseEmitter emitter, Exception e) {
        try {
            emitter.send(SseEmitter.event().data("요청 처리 중 오류가 발생했습니다."));
        } catch (Exception ignored) {
        }
        emitter.completeWithError(e);
    }

    @PreDestroy
    public void shutdown() {
        // 현재 ExecutorService 사용하지 않으므로 불필요하지만,
        // 추후 확장 가능성을 대비해 템플릿 유지
    }
}
