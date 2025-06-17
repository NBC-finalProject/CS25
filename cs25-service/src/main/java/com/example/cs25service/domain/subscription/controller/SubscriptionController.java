package com.example.cs25service.domain.subscription.controller;

import com.example.cs25common.global.dto.ApiResponse;
import com.example.cs25service.domain.security.dto.AuthUser;
import com.example.cs25service.domain.subscription.dto.SubscriptionInfoDto;
import com.example.cs25service.domain.subscription.dto.SubscriptionRequest;
import com.example.cs25service.domain.subscription.dto.SubscriptionResponseDto;
import com.example.cs25service.domain.subscription.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping("/{subscriptionId}")
    public ApiResponse<SubscriptionInfoDto> getSubscription(
        @PathVariable("subscriptionId") Long subscriptionId
    ) {
        return new ApiResponse<>(
            200,
            subscriptionService.getSubscription(subscriptionId)
        );
    }

    @PostMapping
    public ApiResponse<SubscriptionResponseDto> createSubscription(
        @RequestBody @Valid SubscriptionRequest request,
        @AuthenticationPrincipal AuthUser authUser
    ) {
        SubscriptionResponseDto subscription = subscriptionService.createSubscription(request,
            authUser);
        return new ApiResponse<>(201,
            new SubscriptionResponseDto(
                subscription.getId(),
                subscription.getCategory(),
                subscription.getStartDate(),
                subscription.getEndDate(),
                subscription.getSubscriptionType()
            ));
    }

    @PatchMapping("/{subscriptionId}")
    public ApiResponse<Void> updateSubscription(
        @PathVariable(name = "subscriptionId") Long subscriptionId,
        @ModelAttribute @Valid SubscriptionRequest request
    ) {
        subscriptionService.updateSubscription(subscriptionId, request);
        return new ApiResponse<>(200);
    }

    @PatchMapping("/{subscriptionId}/cancel")
    public ApiResponse<Void> cancelSubscription(
        @PathVariable(name = "subscriptionId") Long subscriptionId
    ) {
        subscriptionService.cancelSubscription(subscriptionId);
        return new ApiResponse<>(200);
    }

    @GetMapping("/email/check")
    public ApiResponse<Boolean> checkEmail(
        @RequestParam("email") String email
    ) {
        subscriptionService.checkEmail(email);
        return new ApiResponse<>(200);
    }
}
