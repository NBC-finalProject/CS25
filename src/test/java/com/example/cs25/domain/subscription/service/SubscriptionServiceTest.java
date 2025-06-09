package com.example.cs25.domain.subscription.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.example.cs25.domain.quiz.entity.QuizCategory;
import com.example.cs25.domain.subscription.dto.SubscriptionInfoDto;
import com.example.cs25.domain.subscription.entity.DayOfWeek;
import com.example.cs25.domain.subscription.entity.Subscription;
import com.example.cs25.domain.subscription.entity.SubscriptionHistory;
import com.example.cs25.domain.subscription.repository.SubscriptionHistoryRepository;
import com.example.cs25.domain.subscription.repository.SubscriptionRepository;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @InjectMocks
    private SubscriptionService subscriptionService;

    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private SubscriptionHistoryRepository subscriptionHistoryRepository;


    private Long subscriptionId = 1L;
    private Subscription subscription;

    @BeforeEach
    void setUp() {
        subscription = Subscription.builder()
            .subscriptionType(Subscription.decodeDays(1))
            .email("test@example.com")
            .startDate(LocalDate.of(2025, 5, 1))
            .endDate(LocalDate.of(2025, 5, 31))
            .category(new QuizCategory(1L, "BACKEND"))
            .build();

        ReflectionTestUtils.setField(subscription, "id", subscriptionId);
    }

    @Test
    void getSubscriptionById_정상조회() {
        // given
        given(subscriptionRepository.findByIdOrElseThrow(subscriptionId))
            .willReturn(subscription);

        // when
        SubscriptionInfoDto dto = subscriptionService.getSubscription(subscriptionId);

        // then
        assertThat(dto.getSubscriptionType()).isEqualTo(Set.of(DayOfWeek.SUNDAY));
        assertThat(dto.getCategory()).isEqualTo("BACKEND");
        assertThat(dto.getPeriod()).isEqualTo(30L);
    }

    @Test
    void cancelSubscription_정상비활성화() {
        // given
        Subscription spy = spy(subscription);
        given(subscriptionRepository.findByIdOrElseThrow(subscriptionId))
            .willReturn(spy);

        // when
        subscriptionService.cancelSubscription(subscriptionId);

        // then
        verify(spy).cancel(); // cancel() 호출되었는지 검증
        verify(subscriptionHistoryRepository).save(any(SubscriptionHistory.class)); // 히스토리 저장 호출 검증
    }
}
