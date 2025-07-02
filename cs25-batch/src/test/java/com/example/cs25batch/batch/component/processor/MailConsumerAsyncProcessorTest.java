package com.example.cs25batch.batch.component.processor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.example.cs25batch.batch.dto.MailDto;
import com.example.cs25batch.batch.service.TodayQuizService;
import com.example.cs25entity.domain.quiz.entity.Quiz;
import com.example.cs25entity.domain.subscription.entity.Subscription;
import com.example.cs25entity.domain.subscription.repository.SubscriptionRepository;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MailConsumerAsyncProcessorTest {
    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private TodayQuizService todayQuizService;

    @Mock
    private Subscription subscription;

    @Mock
    private Quiz quiz;

    private MailConsumerAsyncProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new MailConsumerAsyncProcessor(subscriptionRepository, todayQuizService);
    }

    @Test
    @DisplayName("유효한_구독이면_MailDto를_반환한다")
    void validSubscription_return_MailDto() throws Exception {
        // given
        Map<String, String> message = Map.of(
            "subscriptionId", "123",
            "recordId", "abc-0"
        );

        when(subscriptionRepository.findByIdOrElseThrow(123L)).thenReturn(subscription);
        when(subscription.isActive()).thenReturn(true);
        when(subscription.isTodaySubscribed()).thenReturn(true);
        when(todayQuizService.getTodayQuizBySubscription(subscription)).thenReturn(quiz);

        // when
        MailDto result = processor.process(message);

        // then
        assertNotNull(result);
        assertEquals(subscription, result.getSubscription());
        assertEquals(quiz, result.getQuiz());
        assertEquals("abc-0", result.getRecordId());
    }

    @Test
    @DisplayName("구독이_비활성화거나_요일불일치이면_null을_반환한다")
    void Subscription_isActive_false_or_days_not_match_thenReturn_null() throws Exception {
        Map<String, String> message = Map.of("subscriptionId", "123", "recordId", "r-1");

        when(subscriptionRepository.findByIdOrElseThrow(123L)).thenReturn(subscription);
        when(subscription.isActive()).thenReturn(false); // 또는 true + isTodaySubscribed() == false

        MailDto result = processor.process(message);

        assertNull(result);
    }
}