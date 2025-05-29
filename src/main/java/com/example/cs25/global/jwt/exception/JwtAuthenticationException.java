package com.example.cs25.global.jwt.exception;

import org.springframework.http.HttpStatus;

public class JwtAuthenticationException extends Throwable {
    private final JwtExceptionCode errorCode;
    private final HttpStatus httpStatus;
    private final String message;

    /**
     * Constructs a new QuizException with the specified error code.
     *
     * Initializes the exception with the provided QuizExceptionCode, setting the corresponding HTTP status and error message.
     *
     * @param errorCode the quiz-specific error code containing HTTP status and message details
     */
    public JwtAuthenticationException(JwtExceptionCode errorCode) {
        this.errorCode = errorCode;
        this.httpStatus = errorCode.getHttpStatus();
        this.message = errorCode.getMessage();
    }
}
