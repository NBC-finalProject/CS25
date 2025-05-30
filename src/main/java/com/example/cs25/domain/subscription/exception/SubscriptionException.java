package com.example.cs25.domain.subscription.exception;

import com.example.cs25.domain.userQuizAnswer.exception.UserQuizAnswerExceptionCode;
import com.example.cs25.global.exception.BaseException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class SubscriptionException extends BaseException {

    private final UserQuizAnswerExceptionCode errorCode;
    private final HttpStatus httpStatus;
    private final String message;

    public SubscriptionException(UserQuizAnswerExceptionCode errorCode) {
        this.errorCode = errorCode;
        this.httpStatus = errorCode.getHttpStatus();
        this.message = errorCode.getMessage();
    }
}
