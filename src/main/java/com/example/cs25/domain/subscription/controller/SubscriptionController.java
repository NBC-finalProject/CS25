package com.example.cs25.domain.subscription.controller;

import com.example.cs25.domain.subscription.dto.SubscriptionInfoDto;
import com.example.cs25.domain.subscription.dto.SubscriptionRequest;
import com.example.cs25.domain.subscription.service.SubscriptionService;
import com.example.cs25.global.dto.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/subscription")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping("/{subscriptionId}")
    public ApiResponse<SubscriptionInfoDto> getSubscription(
        @PathVariable Long subscriptionId
    ){
        return new ApiResponse<>(
            200,
            subscriptionService.getSubscription(subscriptionId)
        );
    }

    @PostMapping
    public ApiResponse<Void> createSubscription(
        @RequestBody @Valid SubscriptionRequest request
    ) {
        subscriptionService.createSubscription(request);
        return new ApiResponse<>(201);
    }

    @PatchMapping("/{subscriptionId}")
    public ApiResponse<Void> updateSubscription(
        @PathVariable(name = "subscriptionId") Long subscriptionId,
        @ModelAttribute @Valid SubscriptionRequest request
    ){
        subscriptionService.updateSubscription(subscriptionId, request);
        return new ApiResponse<>(200);
    }

    @PatchMapping("/{subscriptionId}/cancel")
    public ApiResponse<Void> cancelSubscription(
        @PathVariable(name = "subscriptionId") Long subscriptionId
    ){
        subscriptionService.cancelSubscription(subscriptionId);
        return new ApiResponse<>(200);
    }
}
