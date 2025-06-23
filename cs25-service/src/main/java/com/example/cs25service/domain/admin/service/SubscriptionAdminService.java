package com.example.cs25service.domain.admin.service;

import com.example.cs25entity.domain.subscription.entity.Subscription;
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


    }
}
