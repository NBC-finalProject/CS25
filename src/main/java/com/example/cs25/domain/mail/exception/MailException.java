package com.example.cs25.domain.mail.exception;

import com.example.cs25.global.exception.BaseException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class MailException extends BaseException {
    private final MailExceptionCode errorCode;
    private final HttpStatus httpStatus;
    private final String message;

    public MailException(MailExceptionCode errorCode) {
        this.errorCode = errorCode;
        this.httpStatus = errorCode.getHttpStatus();
        this.message = errorCode.getMessage();
    }
}
