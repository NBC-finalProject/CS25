package com.example.cs25service.domain.quiz.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor
public class TodayQuizResponseDto {
	private final String question;
	private final String choice1;
	private final String choice2;
	private final String choice3;
	private final String choice4;

	private final String answerNumber;
	private final String commentary;
}
