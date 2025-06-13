package com.example.cs25.domain.userQuizAnswer.controller;

import com.example.cs25.domain.userQuizAnswer.dto.SelectionRateResponseDto;
import com.example.cs25.domain.userQuizAnswer.dto.UserQuizAnswerRequestDto;
import com.example.cs25.domain.userQuizAnswer.service.UserQuizAnswerService;
import com.example.cs25.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/quizzes")
@RequiredArgsConstructor
public class UserQuizAnswerController {

    private final UserQuizAnswerService userQuizAnswerService;

    @PostMapping("/{quizId}")
    public ApiResponse<String> answerSubmit(
        @PathVariable("quizId") Long quizId,
        @RequestBody UserQuizAnswerRequestDto requestDto
    ) {

        userQuizAnswerService.answerSubmit(quizId, requestDto);

        return new ApiResponse<>(200, "답안이 제출 되었습니다.");
    }

    @GetMapping("/{quizId}/select-rate")
    public ApiResponse<SelectionRateResponseDto> getSelectionRateByOption(@PathVariable Long quizId){
        return new ApiResponse<>(200, userQuizAnswerService.getSelectionRateByOption(quizId));
    }
}
