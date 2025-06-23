    package com.example.cs25service.domain.ai.service;

    import com.example.cs25service.domain.ai.dto.request.FeedbackRequest;
    import jakarta.annotation.PostConstruct;
    import jakarta.annotation.PreDestroy;
    import java.io.IOException;
    import java.util.concurrent.BlockingQueue;
    import java.util.concurrent.ExecutorService;
    import java.util.concurrent.Executors;
    import java.util.concurrent.LinkedBlockingQueue;
    import java.util.concurrent.TimeUnit;
    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.stereotype.Service;
    import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

    @Slf4j
    @Service
    @RequiredArgsConstructor
    public class AiFeedbackQueueService {

        private final AiFeedbackStreamProcessor processor;
        private final BlockingQueue<FeedbackRequest> queue = new LinkedBlockingQueue<>(100);
        private final ExecutorService executor = Executors.newSingleThreadExecutor(
            r -> new Thread(r, "ai-feedback-processor")
        );
        private volatile boolean running = true;

        @PostConstruct
        public void initWorker() {
            executor.submit(this::processQueue);
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
            while (running) {
                try {
                    FeedbackRequest request = queue.poll(1, TimeUnit.SECONDS);
                    if (request != null) {
                        processor.stream(request.answerId(), request.emitter());
                    }
                } catch (Exception e) {
                    log.error("Error processing feedback request", e);
                }
            }
        }

        @PreDestroy
        public void shutdown() {
            running = false;
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
