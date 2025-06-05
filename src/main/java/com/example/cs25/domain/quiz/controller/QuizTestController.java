package com.example.cs25.domain.quiz.controller;

import com.example.cs25.domain.quiz.service.QuizAccuracyService;
import com.example.cs25.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class QuizTestController {

    private final QuizAccuracyService accuracyService;

    @GetMapping("/accuracyTest")
    public ApiResponse<Void> accuracyTest() {
        accuracyService.calculateAndCacheAllQuizAccuracies();
        return new ApiResponse<>(200);
    }
}
