package com.example.cs25.domain.ai.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AiExceptionCode {

    NOT_FOUND_QUIZ(false, HttpStatus.NOT_FOUND, "해당 퀴즈를 찾을 수 없습니다"),
    NOT_FOUND_ANSWER(false, HttpStatus.NOT_FOUND, "해당 답변을 찾을 수 없습니다"),
    UNAUTHORIZED(false, HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
    INTERNAL_SERVER_ERROR(false, HttpStatus.INTERNAL_SERVER_ERROR, "AI 채점 서버 오류");

    private final boolean isSuccess;
    private final HttpStatus httpStatus;
    private final String message;
}
