package com.example.cs25service.domain.quiz.controller;

import com.example.cs25common.global.dto.ApiResponse;
import com.example.cs25service.domain.quiz.dto.TodayQuizResponseDto;
import com.example.cs25service.domain.quiz.service.QuizPageService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class QuizPageController {

    private final QuizPageService quizPageService;

    @GetMapping("/todayQuiz")
    public ApiResponse<TodayQuizResponseDto> showTodayQuizPage(
        HttpServletResponse response,
        @RequestParam("subscriptionId") Long subscriptionId,
        @RequestParam("quizId") Long quizId,
        Model model
    ) {
        Cookie cookie = new Cookie("subscriptionId", subscriptionId.toString());
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        response.addCookie(cookie);

        return new ApiResponse<>(
            200,
            quizPageService.setTodayQuizPage(quizId, model)
        );
    }
}
