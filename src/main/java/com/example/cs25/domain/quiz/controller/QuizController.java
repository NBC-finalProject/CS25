package com.example.cs25.domain.quiz.controller;

import com.example.cs25.domain.quiz.dto.QuizResponseDto;
import com.example.cs25.domain.quiz.entity.QuizFormatType;
import com.example.cs25.domain.quiz.service.QuizService;
import com.example.cs25.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
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
        @RequestParam("formatType") QuizFormatType formatType
    ) {
        if (file.isEmpty()) {
            return new ApiResponse<>(400, "파일이 비어있습니다.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals(MediaType.APPLICATION_JSON_VALUE)) {
            return new ApiResponse<>(400, "JSON 파일만 업로드 가능합니다.");
        }

        quizService.uploadQuizJson(file, categoryType, formatType);
        return new ApiResponse<>(200, "문제 등록 성공");
    }

    @GetMapping("/{quizId}")
    public ApiResponse<QuizResponseDto> getQuizDetail(@PathVariable Long quizId){
        return new ApiResponse<>(200, quizService.getQuizDetail(quizId));
    }
}
