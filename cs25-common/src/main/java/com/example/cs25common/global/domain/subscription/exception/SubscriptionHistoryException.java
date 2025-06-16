package com.example.cs25common.global.domain.subscription.exception;


import com.example.cs25common.global.exception.BaseException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class SubscriptionHistoryException extends BaseException {

    private final SubscriptionHistoryExceptionCode errorCode;
    private final HttpStatus httpStatus;
    private final String message;

    public SubscriptionHistoryException(SubscriptionHistoryExceptionCode errorCode) {
        this.errorCode = errorCode;
        this.httpStatus = errorCode.getHttpStatus();
        this.message = errorCode.getMessage();
    }
}
