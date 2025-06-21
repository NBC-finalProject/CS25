package com.example.cs25service.domain.quiz.controller;

import com.example.cs25common.global.dto.ApiResponse;
import com.example.cs25entity.domain.quiz.enums.QuizFormatType;
import com.example.cs25service.domain.quiz.dto.QuizResponseDto;
import com.example.cs25service.domain.quiz.service.QuizService;
import com.example.cs25service.domain.security.dto.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/quizzes")
public class QuizController {

    private final QuizService quizService;

    @PostMapping("/upload")
    public ApiResponse<String> uploadQuizByJsonFile(
        @RequestParam("file") MultipartFile file,
        @RequestParam("categoryType") String categoryType,
        @RequestParam("formatType") QuizFormatType formatType,
        @AuthenticationPrincipal AuthUser authUser
    ) {
        if (file.isEmpty()) {
            return new ApiResponse<>(400, "파일이 비어있습니다.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals(MediaType.APPLICATION_JSON_VALUE)) {
            return new ApiResponse<>(400, "JSON 파일만 업로드 가능합니다.");
        }

        quizService.uploadQuizJson(authUser, file, categoryType, formatType);
        return new ApiResponse<>(200, "문제 등록 성공");
    }

    @GetMapping("/{quizId}")
    public ApiResponse<QuizResponseDto> getQuizDetail(@PathVariable Long quizId) {
        return new ApiResponse<>(200, quizService.getQuizDetail(quizId));
    }
}
