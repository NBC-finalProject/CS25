package com.example.cs25.domain.oauth.exception;

import org.springframework.http.HttpStatus;

public class aaException {
    private final aaExceptionCode errorCode;
    private final HttpStatus httpStatus;
    private final String message;

    public aaException(aaExceptionCode errorCode) {
        this.errorCode = errorCode;
        this.httpStatus = errorCode.getHttpStatus();
        this.message = errorCode.getMessage();
    }
}
