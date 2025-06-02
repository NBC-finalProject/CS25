package com.example.cs25.domain.oauth.exception;

import org.springframework.http.HttpStatus;

public class OAuthException {
    private final OAuthExceptionCode errorCode;
    private final HttpStatus httpStatus;
    private final String message;

    public OAuthException(OAuthExceptionCode errorCode) {
        this.errorCode = errorCode;
        this.httpStatus = errorCode.getHttpStatus();
        this.message = errorCode.getMessage();
    }
}
