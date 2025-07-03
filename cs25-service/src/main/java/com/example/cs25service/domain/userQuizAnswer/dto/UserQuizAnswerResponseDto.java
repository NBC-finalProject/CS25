package com.example.cs25service.domain.userQuizAnswer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserQuizAnswerResponseDto {
    private final Long userQuizAnswerId; // 답변 id
    private final String question; // 문제
    private final String answer; // 문제 모범답안
    private final String commentary; // 문제 해설
    private Boolean isCorrect; // 문제 맞춤 여부

    private final String userAnswer; // 사용자가 답변한 텍스트
    private final String aiFeedback; // 서술형의 경우, AI 피드백
    private final boolean duplicated; // 중복답변 여부
}
