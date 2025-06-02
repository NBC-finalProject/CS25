package com.example.cs25.domain.quiz.controller;

import com.example.cs25.domain.quiz.entity.QuizCategoryType;
import com.example.cs25.domain.quiz.service.QuizCategoryService;
import com.example.cs25.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class QuizCategoryController {

    private final QuizCategoryService quizCategoryService;

    @PostMapping("/quiz-categories")
    public ApiResponse<String> createQuizCategory(
        @RequestParam QuizCategoryType categoryType
    ) {
        quizCategoryService.createQuizCategory(categoryType);
        return new ApiResponse<>(200, "카테고리 등록 성공");
    }

}
