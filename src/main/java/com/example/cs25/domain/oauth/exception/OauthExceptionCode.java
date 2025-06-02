package com.example.cs25.domain.oauth.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OAuthExceptionCode {

    NOT_FOUND_EVENT(false, HttpStatus.NOT_FOUND, "해당 이벤트를 찾을 수 없습니다");

    private final boolean isSuccess;
    private final HttpStatus httpStatus;
    private final String message;
}

