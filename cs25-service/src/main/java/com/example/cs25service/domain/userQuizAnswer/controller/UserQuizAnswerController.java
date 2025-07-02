package com.example.cs25service.domain.userQuizAnswer.controller;

import com.example.cs25common.global.dto.ApiResponse;
import com.example.cs25service.domain.userQuizAnswer.dto.*;
import com.example.cs25service.domain.userQuizAnswer.service.UserQuizAnswerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/quizzes")
@RequiredArgsConstructor
public class UserQuizAnswerController {

    private final UserQuizAnswerService userQuizAnswerService;

    // 정답 제출
    @PostMapping("/{quizSerialId}")
    public ApiResponse<UserQuizAnswerResponseDto> submitAnswer(
        @PathVariable("quizSerialId") String quizSerialId,
        @RequestBody UserQuizAnswerRequestDto requestDto
    ) {
        return new ApiResponse<>(200, userQuizAnswerService.submitAnswer(quizSerialId, requestDto));
    }

    // 객관식 or 주관식 채점
    @PostMapping("/evaluate/{userQuizAnswerId}")
    public ApiResponse<UserQuizAnswerResponseDto> evaluateAnswer(
        @PathVariable("userQuizAnswerId") Long userQuizAnswerId
    ){
        return new ApiResponse<>(200, userQuizAnswerService.evaluateAnswer(userQuizAnswerId));
    }

    // 특정 퀴즈의 선택률을 계산
    @GetMapping("/{quizSerialId}/select-rate")
    public ApiResponse<SelectionRateResponseDto> calculateSelectionRateByOption(
        @PathVariable("quizSerialId") String quizSerialId) {
        return new ApiResponse<>(200, userQuizAnswerService.calculateSelectionRateByOption(quizSerialId));
    }
}
