package com.example.cs25service.domain.quiz.controller;

import com.example.cs25common.global.dto.ApiResponse;
import com.example.cs25service.domain.quiz.service.QuizAccuracyCalculateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class QuizTestController {

    private final QuizAccuracyCalculateService accuracyService;

    @GetMapping("/accuracyTest")
    public ApiResponse<Void> accuracyTest() {
        accuracyService.calculateAndCacheAllQuizAccuracies();
        return new ApiResponse<>(200);
    }

//    @GetMapping("/accuracyTest/getTodayQuiz/{subscriptionId}")
//    public ApiResponse<QuizDto> getTodayQuiz(
//        @PathVariable(name = "subscriptionId") Long subscriptionId
//    ) {
//        return new ApiResponse<>(200, accuracyService.getTodayQuiz(subscriptionId));
//    }

}
