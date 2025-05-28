package com.example.cs25.domain.oauth.exception;

import org.springframework.http.HttpStatus;

public class OauthException {
    private final OauthExceptionCode errorCode;
    private final HttpStatus httpStatus;
    private final String message;

    public OauthException(OauthExceptionCode errorCode) {
        this.errorCode = errorCode;
        this.httpStatus = errorCode.getHttpStatus();
        this.message = errorCode.getMessage();
    }
}
