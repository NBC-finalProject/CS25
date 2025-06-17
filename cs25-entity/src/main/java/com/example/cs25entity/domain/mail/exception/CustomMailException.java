package com.example.cs25entity.domain.mail.exception;

import com.example.cs25common.global.exception.BaseException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomMailException extends BaseException {

    private final MailExceptionCode errorCode;
    private final HttpStatus httpStatus;
    private final String message;

    /**
     * Constructs a new MailException with the specified mail error code.
     * <p>
     * Initializes the exception's HTTP status and message based on the provided MailExceptionCode.
     *
     * @param errorCode the mail-specific error code containing error details
     */
    public CustomMailException(MailExceptionCode errorCode) {
        this.errorCode = errorCode;
        this.httpStatus = errorCode.getHttpStatus();
        this.message = errorCode.getMessage();
    }
}
