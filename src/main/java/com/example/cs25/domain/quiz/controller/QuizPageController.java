package com.example.cs25.domain.quiz.controller;

import com.example.cs25.domain.quiz.service.QuizPageService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class QuizPageController {

    private final QuizPageService quizPageService;

    @GetMapping("/todayQuiz")
    public String showTodayQuizPage(
        HttpServletResponse response,
        @RequestParam("subscriptionId") Long subscriptionId,
        @RequestParam("quizId") Long quizId,
        Model model
    ) {
        Cookie cookie = new Cookie("subscriptionId", subscriptionId.toString());
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        response.addCookie(cookie);

        quizPageService.setTodayQuizPage(quizId, model);

        return "quiz";
    }
}
