package com.example.cs25service.domain.quiz.controller;

import com.example.cs25common.global.dto.ApiResponse;
import com.example.cs25service.domain.quiz.dto.QuizCategoryRequestDto;
import com.example.cs25service.domain.quiz.service.QuizCategoryService;
import com.example.cs25service.domain.security.dto.AuthUser;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class QuizCategoryController {

    private final QuizCategoryService quizCategoryService;

    @GetMapping("/quiz-categories")
    public ApiResponse<List<String>> getQuizCategories() {
        return new ApiResponse<>(200, quizCategoryService.getParentQuizCategoryList());
    }

    @PostMapping("/quiz-categories")
    public ApiResponse<String> createQuizCategory(
        @Valid @RequestBody QuizCategoryRequestDto request,
        @AuthenticationPrincipal AuthUser authUser
    ) {
        quizCategoryService.createQuizCategory(authUser, request);
        return new ApiResponse<>(200, "카테고리 등록 성공");
    }

}
