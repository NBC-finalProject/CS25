package com.example.cs25service.domain.quiz.controller;

import com.example.cs25common.global.dto.ApiResponse;
import com.example.cs25service.domain.quiz.dto.QuizCategoryRequestDto;
import com.example.cs25service.domain.quiz.dto.QuizCategoryResponseDto;
import com.example.cs25service.domain.quiz.service.QuizCategoryService;
import com.example.cs25service.domain.security.dto.AuthUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/quiz-categories")
@RestController
@RequiredArgsConstructor
public class QuizCategoryController {

    private final QuizCategoryService quizCategoryService;

    @GetMapping()
    public ApiResponse<List<String>> getQuizCategories() {
        return new ApiResponse<>(200, quizCategoryService.getParentQuizCategoryList());
    }

}
