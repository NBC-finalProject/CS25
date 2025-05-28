package com.example.cs25.domain.quiz.exception;

import com.example.cs25.global.exception.BaseException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class QuizException extends BaseException {
    private final QuizExceptionCode errorCode;
    private final HttpStatus httpStatus;
    private final String message;

    public QuizException(QuizExceptionCode errorCode) {
        this.errorCode = errorCode;
        this.httpStatus = errorCode.getHttpStatus();
        this.message = errorCode.getMessage();
    }
}
