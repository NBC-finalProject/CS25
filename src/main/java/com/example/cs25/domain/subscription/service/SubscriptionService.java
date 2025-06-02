package com.example.cs25.domain.subscription.service;

import com.example.cs25.domain.subscription.dto.SubscriptionInfoDto;
import com.example.cs25.domain.subscription.entity.Subscription;
import com.example.cs25.domain.subscription.exception.SubscriptionException;
import com.example.cs25.domain.subscription.exception.SubscriptionExceptionCode;
import com.example.cs25.domain.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionInfoDto getSubscription(Long subscriptionId) {

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
            .orElseThrow(() ->
                new SubscriptionException(SubscriptionExceptionCode.NOT_FOUND_SUBSCRIPTION_ERROR));

        //구독 시작, 구독 종료 날짜 기반으로 구독 기간 계산

        return SubscriptionInfoDto.builder()
            .subscriptionType(Subscription.decodeDays(subscription.getSubscriptionType()))
            .category(subscription.getCategory())
            build();
    }
}
