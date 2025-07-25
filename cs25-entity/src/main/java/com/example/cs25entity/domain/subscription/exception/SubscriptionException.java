package com.example.cs25entity.domain.subscription.exception;


import com.example.cs25common.global.exception.BaseException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class SubscriptionException extends BaseException {

    private final SubscriptionExceptionCode errorCode;
    private final HttpStatus httpStatus;
    private final String message;

    public SubscriptionException(SubscriptionExceptionCode errorCode) {
        this.errorCode = errorCode;
        this.httpStatus = errorCode.getHttpStatus();
        this.message = errorCode.getMessage();
    }
}
