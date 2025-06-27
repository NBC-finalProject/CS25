package com.example.cs25batch.batch.service;

import com.example.cs25entity.domain.subscription.dto.SubscriptionMailTargetDto;
import com.example.cs25entity.domain.subscription.repository.SubscriptionRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BatchSubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    @Transactional(readOnly = true)
    public List<SubscriptionMailTargetDto> getTodaySubscriptions() {
        LocalDate today = LocalDate.now();
        int dayIndex = today.getDayOfWeek().getValue() % 7;
        int todayBit = 1 << dayIndex;

        return subscriptionRepository.findAllTodaySubscriptions(today, todayBit);
    }
}
