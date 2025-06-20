package com.example.cs25service.domain.userQuizAnswer.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserQuizAnswerRequestDto {
    private String answer;
    private Long subscriptionId;
}
