package com.example.cs25.domain.quiz.controller;

import java.util.List;

import com.example.cs25.domain.quiz.service.QuizCategoryService;
import com.example.cs25.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class QuizCategoryController {

    private final QuizCategoryService quizCategoryService;

    @GetMapping("/quiz-categories")
    public ApiResponse<List<String>> getQuizCategories() {
        return new ApiResponse<>(200, quizCategoryService.getQuizCategoryList());
    }

    @PostMapping("/quiz-categories")
    public ApiResponse<String> createQuizCategory(
        @RequestParam("categoryType") String categoryType
    ) {
        quizCategoryService.createQuizCategory(categoryType);
        return new ApiResponse<>(200, "카테고리 등록 성공");
    }

}
