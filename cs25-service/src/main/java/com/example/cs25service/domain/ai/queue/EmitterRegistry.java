package com.example.cs25service.domain.ai.queue;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
public class EmitterRegistry {

    private final ConcurrentHashMap<Long, SseEmitter> emitterMap = new ConcurrentHashMap<>();

    public void register(Long answerId, SseEmitter emitter) {
        // 기존 emitter 가 있다면 정리
        SseEmitter existing = emitterMap.put(answerId, emitter);
        if (existing != null) {
            existing.complete();
        }

        // emitter 완료/오류 시 자동 제거
        emitter.onCompletion(() -> remove(answerId));
        emitter.onTimeout(() -> remove(answerId));
        emitter.onError((throwable) -> remove(answerId));
    }

    public SseEmitter get(Long answerId) {
        return emitterMap.get(answerId);
    }

    public void remove(Long answerId) {
        emitterMap.remove(answerId);
    }

    public int size() {
        return emitterMap.size();
    }

}
