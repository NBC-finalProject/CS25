package com.example.cs25service.domain.admin.service;

import com.example.cs25entity.domain.subscription.entity.Subscription;
import com.example.cs25entity.domain.subscription.exception.SubscriptionException;
import com.example.cs25entity.domain.subscription.exception.SubscriptionExceptionCode;
import com.example.cs25entity.domain.subscription.repository.SubscriptionRepository;
import com.example.cs25service.domain.admin.dto.response.SubscriptionPageResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubscriptionAdminService {

    private final SubscriptionRepository subscriptionRepository;

    public Page<SubscriptionPageResponseDto> getAdminSubscriptions(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);

        Page<Subscription> subscriptionPage = subscriptionRepository.findAllByOrderByIdAsc(
            pageable);

        return subscriptionPage.map(subscription ->
            SubscriptionPageResponseDto.builder()
                .id(subscription.getId())
                .serialId(subscription.getSerialId())
                .category(subscription.getCategory().getCategoryType())
                .email(subscription.getEmail())
                .isActive(subscription.isActive())
                .subscriptionType(Subscription.decodeDays(subscription.getSubscriptionType()))
                .build()
        );
    }

    /**
     * 구독자 개별 조회
     * @param subscriptionId
     * @return
     */
    public SubscriptionPageResponseDto getSubscription(Long subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId).orElseThrow(
                () -> new SubscriptionException(SubscriptionExceptionCode.NOT_FOUND_SUBSCRIPTION_ERROR)
        );

        return SubscriptionPageResponseDto.builder()
                .id(subscription.getId())
                .category(subscription.getCategory().getCategoryType())
                .email(subscription.getEmail())
                .isActive(subscription.isActive())
                .serialId(subscription.getSerialId())
                .subscriptionType(Subscription.decodeDays(subscription.getSubscriptionType()))
                .build();
    }

    /**
     * 구독 취소
     * @param subscriptionId
     */
    public void deleteSubscription(Long subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId).orElseThrow(
                () -> new SubscriptionException(SubscriptionExceptionCode.NOT_FOUND_SUBSCRIPTION_ERROR)
        );
        subscription.updateDisable();
    }
}
