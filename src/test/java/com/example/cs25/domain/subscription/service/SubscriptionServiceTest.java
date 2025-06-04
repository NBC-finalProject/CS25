package com.example.cs25.domain.subscription.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.example.cs25.domain.quiz.entity.QuizCategory;
import com.example.cs25.domain.subscription.dto.SubscriptionInfoDto;
import com.example.cs25.domain.subscription.entity.DayOfWeek;
import com.example.cs25.domain.subscription.entity.Subscription;
import com.example.cs25.domain.subscription.exception.SubscriptionException;
import com.example.cs25.domain.subscription.repository.SubscriptionRepository;
import java.time.LocalDate;
import java.util.Optional;
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

    private Long subscriptionId = 1L;
    private Subscription subscription;

    @BeforeEach
    void setUp() {
        subscription = Subscription.builder()
            .subscriptionType(1)
            .startDate(LocalDate.of(2024, 1, 1))
            .endDate(LocalDate.of(2024, 1, 31))
            .category(new QuizCategory(1L, "BACKEND"))
            .build();

        ReflectionTestUtils.setField(subscription, "id", subscriptionId);
    }

    @Test
    void getSubscriptionById_정상조회() {
        // given
        given(subscriptionRepository.findById(subscriptionId))
            .willReturn(Optional.of(subscription));

        // when
        SubscriptionInfoDto dto = subscriptionService.getSubscription(subscriptionId);

        // then
        assertThat(dto.getSubscriptionType()).isEqualTo(Set.of(DayOfWeek.SUNDAY));
        assertThat(dto.getCategoryName()).isEqualTo("BACKEND");
        assertThat(dto.getPeriod()).isEqualTo(30L);
    }


    @Test
    void getSubscriptionById_존재하지않을때_예외() {
        // given
        Long subscriptionId = 999L;
        given(subscriptionRepository.findById(subscriptionId)).willReturn(Optional.empty());

        // when & then
        assertThrows(SubscriptionException.class, () ->
            subscriptionService.getSubscription(subscriptionId));
    }

    @Test
    void getSubscriptionByEmail_정상조회() {
        // given
        String email = "test@example.com";
        Subscription subscription = Subscription.builder().email(email).build();

        given(subscriptionRepository.findByEmail(email))
            .willReturn(Optional.of(subscription));

        // when
        Subscription result = subscriptionService.getSubscription(email);

        // then
        assertThat(result.getEmail()).isEqualTo(email);
    }

    @Test
    void getSubscriptionByEmail_존재하지않을때_예외() {
        // given
        String email = "noexist@example.com";
        given(subscriptionRepository.findByEmail(email)).willReturn(Optional.empty());

        // when & then
        assertThrows(SubscriptionException.class, () ->
            subscriptionService.getSubscription(email));
    }

    @Test
    void disableSubscription_정상비활성화() {
        // given
        Subscription spy = spy(subscription);
        given(subscriptionRepository.findById(subscriptionId))
            .willReturn(Optional.of(spy));

        // when
        subscriptionService.disableSubscription(subscriptionId);

        // then
        verify(spy).updateDisableSubscription();
    }

    @Test
    void disableSubscription_존재하지않을때_예외() {
        // given
        Long subscriptionId = 999L;
        given(subscriptionRepository.findById(subscriptionId)).willReturn(Optional.empty());

        // when & then
        assertThrows(SubscriptionException.class, () ->
            subscriptionService.disableSubscription(subscriptionId));
    }
}
