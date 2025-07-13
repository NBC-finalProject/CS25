package com.example.cs25service.domain.subscription.controller;

import com.example.cs25common.global.dto.ApiResponse;
import com.example.cs25service.domain.mail.service.TodayQuizService;
import com.example.cs25service.domain.security.dto.AuthUser;
import com.example.cs25service.domain.subscription.dto.SubscriptionInfoDto;
import com.example.cs25service.domain.subscription.dto.SubscriptionRequestDto;
import com.example.cs25service.domain.subscription.dto.SubscriptionResponseDto;
import com.example.cs25service.domain.subscription.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final TodayQuizService todayQuizService;

    @GetMapping("/{subscriptionId}")
    public ApiResponse<SubscriptionInfoDto> getSubscription(
        @PathVariable("subscriptionId") String subscriptionId
    ) {
        return new ApiResponse<>(
            200,
            subscriptionService.getSubscription(subscriptionId)
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SubscriptionResponseDto> createSubscription(
        @RequestBody @Valid SubscriptionRequestDto request,
        @AuthenticationPrincipal AuthUser authUser
    ) {
        SubscriptionResponseDto result = subscriptionService.createSubscription(request, authUser);
        todayQuizService.sendQuizMail(result.getId());
        return new ApiResponse<>(201,
            result);
    }

    @PatchMapping("/{subscriptionId}")
    public ApiResponse<Void> updateSubscription(
        @PathVariable(name = "subscriptionId") String subscriptionId,
        @RequestBody @Valid SubscriptionRequestDto request
    ) {
        subscriptionService.updateSubscription(subscriptionId, request);
        return new ApiResponse<>(200);
    }

    @PatchMapping("/{subscriptionId}/cancel")
    public ApiResponse<Void> cancelSubscription(
        @PathVariable(name = "subscriptionId") String subscriptionId
    ) {
        subscriptionService.cancelSubscription(subscriptionId);
        return new ApiResponse<>(200);
    }

    @GetMapping("/email/check")
    public ApiResponse<Void> checkEmail(
        @RequestParam("email") String email
    ) {
        subscriptionService.checkEmail(email);
        return new ApiResponse<>(200);
    }
}
