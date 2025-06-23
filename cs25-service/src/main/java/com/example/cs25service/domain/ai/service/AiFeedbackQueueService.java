package com.example.cs25service.domain.ai.service;

import com.example.cs25service.domain.ai.dto.request.FeedbackRequest;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
public class AiFeedbackQueueService {

    private final AiService aiService;
    private final BlockingQueue<FeedbackRequest> queue = new LinkedBlockingQueue<>(100);

    @PostConstruct
    public void initWorker() {
        Executors.newSingleThreadExecutor().submit(this::processQueue);
    }

    public void enqueue(FeedbackRequest request) {
        boolean offered = queue.offer(request);
        if (!offered) {
            try {
                request.emitter().send(SseEmitter.event().data("현재 요청이 너무 많습니다. 잠시 후 다시 시도해주세요."));
                request.emitter().complete();
            } catch (IOException e) {
                request.emitter().completeWithError(e);
            }
        }
    }

    private void processQueue() {
        while (true) {
            try {
                FeedbackRequest request = queue.take();
                aiService.streamFeedbackInternal(request.answerId(), request.emitter());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}