package com.example.cs25.domain.ai.controller;

import com.example.cs25.domain.ai.dto.response.AiFeedbackResponse;
import com.example.cs25.domain.ai.service.AiQuestionGeneratorService;
import com.example.cs25.domain.ai.service.AiService;
import com.example.cs25.domain.quiz.entity.Quiz;
import com.example.cs25.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/quizzes")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;
    private final AiQuestionGeneratorService aiQuestionGeneratorService;

    @GetMapping("/{quizId}/feedback")
    public ResponseEntity<?> getFeedback(
            @PathVariable Long quizId,
            @RequestHeader(value = "subscriptionId") Long subscriptionId) {

        AiFeedbackResponse response = aiService.getFeedback(quizId, subscriptionId);
        return ResponseEntity.ok(new ApiResponse<>(200, response));
    }

    @GetMapping("/generate")
    public ResponseEntity<?> generateQuiz() {
        Quiz quiz = aiQuestionGeneratorService.generateQuestionFromContext();
        return ResponseEntity.ok(new ApiResponse<>(200, quiz));
    }
}