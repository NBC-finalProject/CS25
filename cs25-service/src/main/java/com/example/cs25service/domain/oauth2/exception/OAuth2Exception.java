package com.example.cs25service.domain.oauth2.exception;

import com.example.cs25common.global.exception.BaseException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

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
