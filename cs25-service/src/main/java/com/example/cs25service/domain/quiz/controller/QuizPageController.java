package com.example.cs25service.domain.quiz.controller;

import com.example.cs25common.global.dto.ApiResponse;
import com.example.cs25service.domain.quiz.dto.TodayQuizResponseDto;
import com.example.cs25service.domain.quiz.service.QuizPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class QuizPageController {

    private final QuizPageService quizPageService;

    @GetMapping("/todayQuiz")
    public ApiResponse<TodayQuizResponseDto> showTodayQuizPage(
        @RequestParam("subscriptionId") String subscriptionId,
        @RequestParam("quizId") String quizId
    ) {

        return new ApiResponse<>(
            200,
            quizPageService.showTodayQuizPage(quizId)
        );
    }
}
