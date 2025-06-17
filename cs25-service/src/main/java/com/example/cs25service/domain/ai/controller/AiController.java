package com.example.cs25service.domain.ai.controller;

import com.example.cs25common.global.dto.ApiResponse;
import com.example.cs25entity.domain.quiz.entity.Quiz;
import com.example.cs25service.domain.ai.dto.response.AiFeedbackResponse;
import com.example.cs25service.domain.ai.service.AiQuestionGeneratorService;
import com.example.cs25service.domain.ai.service.AiService;
import com.example.cs25service.domain.ai.service.FileLoaderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/quizzes")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;
    private final AiQuestionGeneratorService aiQuestionGeneratorService;
    private final FileLoaderService fileLoaderService;

    @PostMapping("/{answerId}/feedback")
    public ResponseEntity<?> getFeedback(@PathVariable(name = "answerId") Long answerId) {
        AiFeedbackResponse response = aiService.getFeedback(answerId);
        return ResponseEntity.ok(new ApiResponse<>(200, response));
    }

    @GetMapping("/generate")
    public ResponseEntity<?> generateQuiz() {
        Quiz quiz = aiQuestionGeneratorService.generateQuestionFromContext();
        return ResponseEntity.ok(new ApiResponse<>(200, quiz));
    }


    @GetMapping("/{dirName}")
    public String loadFiles(@PathVariable("dirName") String dirName) {
        fileLoaderService.loadAndSaveFiles(dirName);
        return "파일 적재 완료!";
    }
}