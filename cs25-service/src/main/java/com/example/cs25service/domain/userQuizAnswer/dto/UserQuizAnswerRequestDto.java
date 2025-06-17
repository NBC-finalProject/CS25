package com.example.cs25service.domain.userQuizAnswer.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserQuizAnswerRequestDto {

    private String answer;
    private Long subscriptionId;

    @Builder
    public UserQuizAnswerRequestDto(String answer, Long subscriptionId) {
        this.answer = answer;
        this.subscriptionId = subscriptionId;
    }
}
