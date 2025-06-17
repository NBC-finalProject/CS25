package com.example.cs25batch.batch.controller;

import com.example.cs25batch.batch.dto.QuizDto;
import com.example.cs25batch.batch.service.TodayQuizService;
import com.example.cs25common.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class QuizTestController {

    private final TodayQuizService accuracyService;

    @GetMapping("/accuracyTest/getTodayQuiz")
    public ApiResponse<QuizDto> getTodayQuiz() {
        return new ApiResponse<>(200, accuracyService.getTodayQuiz(1L));
    }

    @GetMapping("/accuracyTest/getTodayQuizNew")
    public ApiResponse<QuizDto> getTodayQuizNew() {
        return new ApiResponse<>(200, accuracyService.getTodayQuizNew(1L));
    }

    @PostMapping("/emails/getTodayQuiz")
    public ApiResponse<String> sendTodayQuiz(
        @RequestParam("subscriptionId") Long subscriptionId
    ) {
        accuracyService.issueTodayQuiz(subscriptionId);
        return new ApiResponse<>(200, "문제 발송 성공");
    }
}
