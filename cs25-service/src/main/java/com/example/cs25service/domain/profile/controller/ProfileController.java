package com.example.cs25service.domain.profile.controller;

import com.example.cs25common.global.dto.ApiResponse;
import com.example.cs25service.domain.profile.dto.ProfileResponseDto;
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

    @GetMapping("wrong-quiz")
    public ApiResponse<ProfileResponseDto> getWrongQuiz(@AuthenticationPrincipal AuthUser authUser){

        return new ApiResponse<>(200, profileService.getWrongQuiz(authUser));
    }
}

