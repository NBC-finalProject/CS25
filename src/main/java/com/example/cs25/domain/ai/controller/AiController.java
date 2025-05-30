package com.example.cs25.domain.ai.controller;

import com.example.cs25.domain.ai.dto.response.AiFeedbackResponse;
import com.example.cs25.domain.ai.service.AiService;
import com.example.cs25.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/quizzes")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @GetMapping("/{quizId}/feedback")
    public ResponseEntity<?> getFeedback(@PathVariable Long quizId){
        AiFeedbackResponse response = aiService.getFeedback(quizId);
        return ResponseEntity.ok(new ApiResponse<>(200, response));
    }
}
