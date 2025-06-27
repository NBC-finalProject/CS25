package com.example.cs25service.domain.ai.dto.request;

import java.util.Objects;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public record FeedbackRequest(
    Long answerId,
    SseEmitter emitter
) {
    public FeedbackRequest {
        Objects.requireNonNull(answerId, "answerId 는 null 값을 가질 수 없습니다.");
        Objects.requireNonNull(emitter, "emitter 는 null 값을 가질 수 없습니다.");
    }
}
