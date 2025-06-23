package com.example.cs25service.domain.ai.dto.request;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public record FeedbackRequest(
    Long answerId,
    SseEmitter emitter
) {}
