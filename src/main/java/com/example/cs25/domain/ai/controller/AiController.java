package com.example.cs25.domain.ai.controller;

import com.example.cs25.domain.ai.dto.response.AiFeedbackResponse;
import com.example.cs25.domain.ai.service.AiService;
import com.example.cs25.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/quizzes")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @GetMapping("/{quizId}/feedback")
    public ResponseEntity<?> getFeedback(
        @PathVariable Long quizId,
        @RequestHeader(value = "subscriptionId") Long subscriptionId) {

        AiFeedbackResponse response = aiService.getFeedback(quizId, subscriptionId);
        return ResponseEntity.ok(new ApiResponse<>(200, response));
    }

}
