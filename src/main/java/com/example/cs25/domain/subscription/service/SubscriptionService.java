package com.example.cs25.domain.subscription.service;

import com.example.cs25.domain.subscription.dto.SubscriptionInfoDto;
import com.example.cs25.domain.subscription.entity.Subscription;
import com.example.cs25.domain.subscription.exception.SubscriptionException;
import com.example.cs25.domain.subscription.exception.SubscriptionExceptionCode;
import com.example.cs25.domain.subscription.repository.SubscriptionRepository;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final VerificationCodeService verificationCodeService;

    public SubscriptionInfoDto getSubscription(Long subscriptionId) {

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
            .orElseThrow(() ->
                new SubscriptionException(SubscriptionExceptionCode.NOT_FOUND_SUBSCRIPTION_ERROR));

        //구독 시작, 구독 종료 날짜 기반으로 구독 기간 계산
        LocalDate start = subscription.getStartDate();
        LocalDate end = subscription.getEndDate();
        long period = ChronoUnit.DAYS.between(start, end);

        return SubscriptionInfoDto.builder()
            .subscriptionType(Subscription.decodeDays(subscription.getSubscriptionType()))
            .category(subscription.getCategory())
            .period(period).build();
    }

    private boolean checkDuplicatedByEmail(String email) {
        Optional<Subscription> subscription = subscriptionRepository.findByEmail(email);
        if (subscription.isPresent()) {
            throw new SubscriptionException(SubscriptionExceptionCode.SUBSCRIPTION_ALREADY_EXIST_ERROR);
        }
        return true;
    }

}
