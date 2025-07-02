package com.example.cs25batch.batch.step;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.cs25batch.batch.service.BatchProducerService;
import com.example.cs25batch.batch.service.BatchSubscriptionService;
import com.example.cs25entity.domain.subscription.dto.SubscriptionMailTargetDto;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

@ExtendWith(MockitoExtension.class)
class MailProducerStepConfigTest {

    @Mock
    private BatchSubscriptionService subscriptionService;

    @Mock
    private BatchProducerService batchProducerService;

    private Tasklet mailProducerTasklet;

    @BeforeEach
    void setUp() {
        MailProducerStepConfig config = new MailProducerStepConfig(subscriptionService, batchProducerService);
        mailProducerTasklet = config.mailProducerTasklet();
    }

    @Test
    @DisplayName("메일발송_대상자를_조회하고_큐에_넣는다")
    void mailProducerTasklet_success() throws Exception {
        // given
        List<SubscriptionMailTargetDto> fakeList = List.of(
            new SubscriptionMailTargetDto(1L, "test1@test.com", "category"),
            new SubscriptionMailTargetDto(2L, "test2@test.com", "category"),
            new SubscriptionMailTargetDto(3L, "test3@test.com", "category")
        );

        when(subscriptionService.getTodaySubscriptions()).thenReturn(fakeList);

        // when
        RepeatStatus status = mailProducerTasklet.execute(null, null);

        // then
        verify(subscriptionService).getTodaySubscriptions();
        verify(batchProducerService).enqueueQuizEmail(1L);
        verify(batchProducerService).enqueueQuizEmail(2L);
        verify(batchProducerService).enqueueQuizEmail(3L);
        assertEquals(RepeatStatus.FINISHED, status);
    }
}