package com.example.cs25service.domain.quiz.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuizCategoryResponseDto {
	private final String main; // 대분류
	private final String sub; // 소분류

	private QuizCategoryResponseDto(String main, String sub) {
		this.main = main;
		this.sub = sub;
	}

	@Builder
	public static QuizCategoryResponseDto of(String main, String sub) {
		return new QuizCategoryResponseDto(main, sub);
	}
}
