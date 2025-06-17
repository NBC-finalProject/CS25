package com.example.cs25service.domain.security.jwt.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum JwtExceptionCode {
    INVALID_SIGNATURE(false, HttpStatus.UNAUTHORIZED, "유효하지 않은 서명입니다."),
    EXPIRED_TOKEN(false, HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    INVALID_TOKEN(false, HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다.");

    private final boolean isSuccess;
    private final HttpStatus httpStatus;
    private final String message;
}
