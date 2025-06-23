package com.example.cs25service.domain.admin.controller;

import com.example.cs25common.global.dto.ApiResponse;
import com.example.cs25service.domain.admin.dto.response.SubscriptionPageResponseDto;
import com.example.cs25service.domain.admin.service.SubscriptionAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class SubscriptionAdminController {

    private final SubscriptionAdminService subscriptionAdminService;

    @GetMapping("/subscription")
    public ApiResponse<Page<SubscriptionPageResponseDto>> getSubscriptionLists(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "30") int size
    ) {
        return new ApiResponse<>(200, subscriptionAdminService.getAdminSubscriptions(page, size));
    }

    // 구독자 개별 조회
    @GetMapping("/subscription/{subscriptionId}")
    public ApiResponse<SubscriptionPageResponseDto> getSubscription(
            @PathVariable Long subscriptionId
    ){
        return new ApiResponse<>(200, subscriptionAdminService.getSubscription(subscriptionId));
    }

    // 구독자 삭제
    @PatchMapping("/subscription/{subscriptionId}")
    public ApiResponse<Void> deleteSubscription(
            @PathVariable Long subscriptionId
    ) {
        subscriptionAdminService.deleteSubscription(subscriptionId);
        return new ApiResponse<>(200);
    }



    
}
