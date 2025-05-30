package com.example.cs25.domain.quiz.controller;

import com.example.cs25.domain.quiz.entity.QuizCategoryType;
import com.example.cs25.domain.quiz.entity.QuizFormatType;
import com.example.cs25.global.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/quizzes")
public class QuizController {
    @PostMapping("/upload")
    public ApiResponse<String> uploadQuizByJsonFile(
        @RequestParam("file")MultipartFile file,
        @RequestParam("categoryType") QuizCategoryType categoryType,
        @RequestParam("formatType") QuizFormatType formatType
    ){
        return new ApiResponse<>(200, "문제 등록 완료");
    }
}
