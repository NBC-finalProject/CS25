package com.example.cs25service.domain.admin.controller;

import com.example.cs25common.global.dto.ApiResponse;
import com.example.cs25service.domain.admin.service.QuizCategoryAdminService;
import com.example.cs25service.domain.quiz.dto.QuizCategoryRequestDto;
import com.example.cs25service.domain.quiz.dto.QuizCategoryResponseDto;
import com.example.cs25service.domain.security.dto.AuthUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/quiz-categories")
public class QuizCategoryAdminController {

    private final QuizCategoryAdminService quizCategoryService;

    @PostMapping
    public ApiResponse<String> createQuizCategory(
        @Valid @RequestBody QuizCategoryRequestDto request,
        @AuthenticationPrincipal AuthUser authUser
    ) {
        quizCategoryService.createQuizCategory(request);
        return new ApiResponse<>(200, "카테고리 등록 성공");
    }

    @PutMapping("/{quizCategoryId}")
    public ApiResponse<QuizCategoryResponseDto> updateQuizCategory(
        @Valid @RequestBody QuizCategoryRequestDto request,
        @NotNull @PathVariable Long quizCategoryId,
        @AuthenticationPrincipal AuthUser authUser
    ){
        return new ApiResponse<>(200, quizCategoryService.updateQuizCategory(quizCategoryId, request));
    }

    @DeleteMapping("/{quizCategoryId}")
    public ApiResponse<String> deleteQuizCategory(
        @NotNull @PathVariable Long quizCategoryId,
        @AuthenticationPrincipal AuthUser authUser
    ){
        quizCategoryService.deleteQuizCategory(quizCategoryId);
        return new ApiResponse<>(200, "카테고리가 삭제되었습니다.");
    }
}
