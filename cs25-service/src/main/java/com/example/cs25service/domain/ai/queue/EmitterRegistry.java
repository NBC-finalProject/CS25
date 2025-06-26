package com.example.cs25service.domain.ai.queue;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
public class EmitterRegistry {

    private final ConcurrentHashMap<Long, SseEmitter> emitterMap = new ConcurrentHashMap<>();

    public void register(Long answerId, SseEmitter emitter) {
        emitterMap.put(answerId, emitter);
    }

    public SseEmitter get(Long answerId) {
        return emitterMap.get(answerId);
    }

    public void remove(Long answerId) {
        emitterMap.remove(answerId);
    }

}
