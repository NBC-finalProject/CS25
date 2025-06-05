package com.example.cs25.domain.oauth.exception;

import org.springframework.http.HttpStatus;

import com.example.cs25.global.exception.BaseException;

import lombok.Getter;

@Getter
public class OAuth2Exception extends BaseException {
    private final OAuth2ExceptionCode errorCode;
    private final HttpStatus httpStatus;
    private final String message;

    public OAuth2Exception(OAuth2ExceptionCode errorCode) {
        this.errorCode = errorCode;
        this.httpStatus = errorCode.getHttpStatus();
        this.message = errorCode.getMessage();
    }
}
