package com.example.cs25service.domain.admin.controller;

import com.example.cs25common.global.dto.ApiResponse;
import com.example.cs25service.domain.admin.dto.response.SubscriptionPageResponseDto;
import com.example.cs25service.domain.admin.service.SubscriptionAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class SubscriptionAdminController {

    private final SubscriptionAdminService subscriptionAdminService;

    @GetMapping
    public ApiResponse<Page<SubscriptionPageResponseDto>> getSubscriptionLists(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "30") int size
    ) {
        return new ApiResponse<>(200, subscriptionAdminService.getAdminSubscriptions(page, size));
    }

    
}
