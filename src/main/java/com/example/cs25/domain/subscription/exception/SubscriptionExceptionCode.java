package com.example.cs25.domain.subscription.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SubscriptionExceptionCode {

    ILLEGAL_SUBSCRIPTION_TYPE_ERROR(false, HttpStatus.BAD_REQUEST, "요일 값이 비정상적입니다."),
    NOT_FOUND_SUBSCRIPTION_ERROR(false, HttpStatus.NOT_FOUND, "구독 정보를 불러올 수 없습니다.");

    private final boolean isSuccess;
    private final HttpStatus httpStatus;
    private final String message;
}
