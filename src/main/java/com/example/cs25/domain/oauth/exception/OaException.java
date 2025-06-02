package com.example.cs25.domain.oauth.exception;

import org.springframework.http.HttpStatus;

public class OaException {
    private final OaExceptionCode errorCode;
    private final HttpStatus httpStatus;
    private final String message;

    public OaException(OaExceptionCode errorCode) {
        this.errorCode = errorCode;
        this.httpStatus = errorCode.getHttpStatus();
        this.message = errorCode.getMessage();
    }
}
