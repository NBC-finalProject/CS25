package com.example.cs25service.domain.admin.controller;

import com.example.cs25common.global.dto.ApiResponse;

import com.example.cs25service.domain.quiz.dto.QuizCategoryRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/admin")
@RequiredArgsConstructor
@RestController
public class SendMailController {
    @PostMapping("/mail-logs/retry")
    public ApiResponse<String> createQuizCategory(
        @Valid @RequestBody QuizCategoryRequestDto request
    ) {
        return new ApiResponse<>(200, "RetryJob Put 성공");
    }
}
