package com.example.cs25service.domain.profile.controller;

import com.example.cs25common.global.dto.ApiResponse;
import com.example.cs25service.domain.profile.dto.ProfileResponseDto;
import com.example.cs25service.domain.profile.dto.ProfileWrongQuizResponseDto;
import com.example.cs25service.domain.profile.dto.UserSubscriptionResponseDto;
import com.example.cs25service.domain.profile.service.ProfileService;
import com.example.cs25service.domain.security.dto.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public ApiResponse<ProfileResponseDto> getProfile(@AuthenticationPrincipal AuthUser authUser){
        return new ApiResponse<>(200, profileService.getProfile(authUser));
    }

    @GetMapping("/subscription")
    public ApiResponse<UserSubscriptionResponseDto> getUserSubscription(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        return new ApiResponse<>(200, profileService.getUserSubscription(authUser));
    }

    @GetMapping("/wrong-quiz")
    public ApiResponse<ProfileWrongQuizResponseDto> getWrongQuiz(@AuthenticationPrincipal AuthUser authUser){

        return new ApiResponse<>(200, profileService.getWrongQuiz(authUser));
    }
}

