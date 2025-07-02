package com.example.cs25batch.batch.service;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

@ExtendWith(MockitoExtension.class)
class BatchProducerServiceTest {
    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private StreamOperations<String, Object, Object> streamOps;

    @InjectMocks
    private BatchProducerService batchProducerService;

    @Test
    @DisplayName("MQ에 데이터가 잘 들어감")
    void enqueueQuizEmail_success() {
        // given
        Long subscriptionId = 42L;
        Map<String, String> expectedMap = Map.of("subscriptionId", "42");

        // redisTemplate.opsForStream()이 streamOps를 반환하도록 mock
        when(redisTemplate.opsForStream()).thenReturn(streamOps);

        // when
        batchProducerService.enqueueQuizEmail(subscriptionId);

        // then
        verify(redisTemplate).opsForStream();
        verify(streamOps).add(eq("quiz-email-stream"), eq(expectedMap));
    }

}