package com.example.cs25service.domain.admin.controller;

import com.example.cs25common.global.dto.ApiResponse;
import com.example.cs25service.domain.admin.dto.response.SubscriptionPageResponseDto;
import com.example.cs25service.domain.admin.service.SubscriptionAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/subscriptions")
public class SubscriptionAdminController {

    private final SubscriptionAdminService subscriptionAdminService;

    @GetMapping
    public ApiResponse<Page<SubscriptionPageResponseDto>> getSubscriptionLists(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "30") int size
    ) {
        return new ApiResponse<>(200, subscriptionAdminService.getAdminSubscriptions(page, size));
    }

    @GetMapping("/{subscriptionId}")
    public ApiResponse<SubscriptionPageResponseDto> getSubscription(
            @PathVariable Long subscriptionId
    ){
        return new ApiResponse<>(200, subscriptionAdminService.getSubscription(subscriptionId));
    }

    @PatchMapping("/{subscriptionId}")
    public ApiResponse<Void> deleteSubscription(
            @PathVariable Long subscriptionId
    ) {
        subscriptionAdminService.deleteSubscription(subscriptionId);
        return new ApiResponse<>(200);
    }
}
