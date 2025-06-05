package com.example.cs25.domain.quiz.controller;

import com.example.cs25.domain.quiz.dto.QuizDto;
import com.example.cs25.domain.quiz.service.TodayQuizService;
import com.example.cs25.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class QuizTestController {

    private final TodayQuizService accuracyService;

    @GetMapping("/accuracyTest")
    public ApiResponse<Void> accuracyTest() {
        accuracyService.calculateAndCacheAllQuizAccuracies();
        return new ApiResponse<>(200);
    }

    @GetMapping("/accuracyTest/getTodayQuiz")
    public ApiResponse<QuizDto> getTodayQuiz() {
        return new ApiResponse<>(200, accuracyService.getTodayQuiz(1L));
    }

    @GetMapping("/accuracyTest/getTodayQuizNew")
    public ApiResponse<QuizDto> getTodayQuizNew() {
        return new ApiResponse<>(200, accuracyService.getTodayQuizNew(1L));
    }
}
