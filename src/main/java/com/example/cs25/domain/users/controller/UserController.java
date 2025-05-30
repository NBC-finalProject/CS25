package com.example.cs25.domain.users.controller;

import com.example.cs25.domain.users.service.UserService;
import com.example.cs25.global.dto.ApiResponse;
import com.example.cs25.global.dto.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ApiResponse<?> getUserProfile(
        @AuthenticationPrincipal AuthUser authUser
    ){
        return new ApiResponse<>(200, userService.getUserProfile(authUser));
    }

}
