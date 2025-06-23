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

    //정답 제출
    @PostMapping("/{quizId}")
    public ApiResponse<Long> answerSubmit(
        @PathVariable("quizId") Long quizId,
        @RequestBody UserQuizAnswerRequestDto requestDto
    ) {
        return new ApiResponse<>(200, userQuizAnswerService.answerSubmit(quizId, requestDto));
    }

    //객관식 or 주관식 채점
    @PostMapping("/simpleAnswer/{userQuizAnswerId}")
    public ApiResponse<CheckSimpleAnswerResponseDto> checkSimpleAnswer(
            @PathVariable("userQuizAnswerId") Long userQuizAnswerId
    ){
        return new ApiResponse<>(200, userQuizAnswerService.checkSimpleAnswer(userQuizAnswerId));
    }

    @GetMapping("/{quizId}/select-rate")
    public ApiResponse<SelectionRateResponseDto> getSelectionRateByOption(
        @PathVariable Long quizId) {
        return new ApiResponse<>(200, userQuizAnswerService.getSelectionRateByOption(quizId));
    }

    @GetMapping("/{userId}/correct-rate")
    public ApiResponse<CategoryUserAnswerRateResponse> getCorrectRateByCategory(
        @PathVariable Long userId
    ){
        return new ApiResponse<>(200, userQuizAnswerService.getUserQuizAnswerCorrectRate(userId));
    }
}
