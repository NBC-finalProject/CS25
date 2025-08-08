package com.example.cs25service.domain.quiz.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TodayQuizResponseDto {
	private final String question;
	private final String choice1; // 객관식 보기 1번 (암호화)
	private final String choice2; // 객관식 보기 2번 (암호화)
	private final String choice3; // 객관식 보기 3번 (암호화)
	private final String choice4; // 객관식 보기 4번 (암호화)

	private final String answerNumber; // 객관식 정답 번호 (암호화)
	private final String answer; // 주관식 모범답안 (암호화)
	private final String commentary; // 객관식 & 주관식 해설 (암호화)

	private final QuizCategoryResponseDto category; // 문제 카테고리 (main, sub)
	private final String quizType; // 객관식 & 주관식 구분
	private final String quizLevel; // 난이도 (HARD, NORMAL, EASY)
}
