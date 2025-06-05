package com.example.cs25.domain.quiz.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum QuizExceptionCode {

    NOT_FOUND_ERROR(false, HttpStatus.NOT_FOUND, "해당 이벤트를 찾을 수 없습니다"),
    QUIZ_CATEGORY_NOT_FOUND_ERROR(false, HttpStatus.NOT_FOUND, "QuizCategory 를 찾을 수 없습니다"),
    QUIZ_CATEGORY_ALREADY_EXISTS_ERROR(false, HttpStatus.CONFLICT, "이미 해당 카테고리가 존재합니다"),
    JSON_PARSING_FAILED_ERROR(false, HttpStatus.BAD_REQUEST, "JSON 파싱 실패"),
    NO_QUIZ_EXISTS_ERROR(false, HttpStatus.NOT_FOUND, "해당 카테고리에 문제가 없습니다."),
    QUIZ_VALIDATION_FAILED_ERROR(false, HttpStatus.BAD_REQUEST, "Quiz 유효성 검증 실패");
    private final boolean isSuccess;
    private final HttpStatus httpStatus;
    private final String message;
}
