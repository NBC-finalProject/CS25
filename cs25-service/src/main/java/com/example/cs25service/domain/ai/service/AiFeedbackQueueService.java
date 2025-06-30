package com.example.cs25service.domain.ai.service;

import com.example.cs25service.domain.ai.config.RedisStreamConfig;
import com.example.cs25service.domain.ai.queue.EmitterRegistry;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiFeedbackQueueService {

    public static final String DEDUPLICATION_SET_KEY = "ai-feedback-dedup-set";
    private final EmitterRegistry emitterRegistry;
    private final RedisTemplate<String, Object> redisTemplate;

    public void enqueue(Long answerId, SseEmitter emitter, String mode) {
        try {
            // Redis Set을 통한 중복 체크
            Long added = redisTemplate.opsForSet()
                .add(DEDUPLICATION_SET_KEY, String.valueOf(answerId));
            if (added == null || added == 0) {
                log.info("Duplicate enqueue prevented for answerId {}", answerId);
                completeWithError(emitter, new IllegalStateException("이미 처리중인 요청입니다."));
                return;
            }

            emitterRegistry.register(answerId, emitter);

            Map<String, Object> message = new HashMap<>();
            message.put("answerId", answerId);
            message.put("mode", mode); // word/sentence 모드 구분 추가

            redisTemplate.opsForStream().add(RedisStreamConfig.STREAM_KEY, message);

        } catch (Exception e) {
            emitterRegistry.remove(answerId);
            redisTemplate.opsForSet()
                .remove(DEDUPLICATION_SET_KEY, String.valueOf(answerId)); // 실패 시 롤백
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
}
