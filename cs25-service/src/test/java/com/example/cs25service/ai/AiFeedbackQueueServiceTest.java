package com.example.cs25service.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.example.cs25service.domain.ai.dto.request.FeedbackRequest;
import com.example.cs25service.domain.ai.service.AiFeedbackQueueService;
import com.example.cs25service.domain.ai.service.AiFeedbackStreamProcessor;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class AiFeedbackQueueServiceTest {

    private AiFeedbackStreamProcessor processor;
    private AiFeedbackQueueService queueService;

    @BeforeEach
    void setUp() {
        processor = mock(AiFeedbackStreamProcessor.class);
        queueService = new AiFeedbackQueueService(processor);
        queueService.initWorker(); // 직접 호출
    }

    @Test
    @DisplayName("큐에 요청이 정상적으로 추가된다")
    void enqueue_success() throws InterruptedException {
        // given
        SseEmitter emitter = new SseEmitter();
        FeedbackRequest request = new FeedbackRequest(1L, emitter);

        // when
        queueService.enqueue(request);

        // then
        // 큐 처리를 위한 약간의 대기
        Thread.sleep(100);
        // preocessor가 호출되었는는 지 검증
        verify(processor, timeout(1000)).stream(1L,emitter);
    }

    @DisplayName("큐가 가득 찼을 때 요청을 거절한다")
    @Test
    void enqueue_rejects_when_queue_full() throws IOException {
        // given
        AiFeedbackStreamProcessor dummyProcessor = mock(AiFeedbackStreamProcessor.class);
        AiFeedbackQueueService queueService = new AiFeedbackQueueService(dummyProcessor);

        SseEmitter rejectedEmitter = mock(SseEmitter.class);
        FeedbackRequest rejectedRequest = new FeedbackRequest(999L, rejectedEmitter);

        // 큐를 최대 크기(100)만큼 채움
        for (int i = 0; i < 100; i++) {
            SseEmitter dummyEmitter = mock(SseEmitter.class);
            FeedbackRequest dummyRequest = new FeedbackRequest((long) i, dummyEmitter);
            queueService.enqueue(dummyRequest); // 내부 queue.offer 성공
        }

        // when
        queueService.enqueue(rejectedRequest); // queue.offer 실패 -> 거절 처리

        // then
        verify(rejectedEmitter).send(any(SseEmitter.SseEventBuilder.class));
        verify(rejectedEmitter).complete();
    }
}
