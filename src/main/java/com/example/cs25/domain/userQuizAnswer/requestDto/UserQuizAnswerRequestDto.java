package com.example.cs25.domain.userQuizAnswer.requestDto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class UserQuizAnswerRequestDto {

    private final String answer;
    private final Long subscriptionId;

    @Builder
    public UserQuizAnswerRequestDto(String answer, Long subscriptionId) {
        this.answer = answer;
        this.subscriptionId = subscriptionId;
    }
}
