package com.example.cs25.domain.ai.exception;
import org.springframework.http.HttpStatus;

public class AiException {
    private final AiExceptionCode errorCode;
    private final HttpStatus httpStatus;
    private final String message;

    public AiException(AiExceptionCode errorCode) {
        this.errorCode = errorCode;
        this.httpStatus = errorCode.getHttpStatus();
        this.message = errorCode.getMessage();
    }
}
