package com.example.cs25.domain.subscription.controller;

import com.example.cs25.domain.subscription.dto.SubscriptionInfoDto;
import com.example.cs25.domain.subscription.service.SubscriptionService;
import com.example.cs25.global.dto.ApiResponse;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping("/subscription/{subscriptionId}")
    public ApiResponse<SubscriptionInfoDto> getSubscription(
        @PathVariable @Positive Long subscriptionId
    ) {
        SubscriptionInfoDto subscription = subscriptionService.getSubscription(subscriptionId);

        return new ApiResponse<>(200, subscription);
    }

    @PatchMapping("/subscription/{subscriptionId}")
    public ApiResponse<Void> disableSubscription(
        @PathVariable @Positive Long subscriptionId
    ) {
        subscriptionService.disableSubscription(subscriptionId);

        return new ApiResponse<>(204, null);
    }
}
