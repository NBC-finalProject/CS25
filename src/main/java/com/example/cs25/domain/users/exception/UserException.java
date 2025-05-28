package com.example.cs25.domain.users.exception;

import com.example.cs25.global.exception.BaseException;
import lombok.Getter;
import org.springframework.http.HttpStatus;



@Getter
public class UserException extends BaseException {
    private final UserExceptionCode errorCode;
    private final HttpStatus httpStatus;
    private final String message;

    /**
     * Constructs a new UserException with the specified user-related error code.
     *
     * Initializes the exception's HTTP status and message based on the provided error code.
     *
     * @param errorCode the user exception code containing error details
     */
    public UserException(UserExceptionCode errorCode) {
        this.errorCode = errorCode;
        this.httpStatus = errorCode.getHttpStatus();
        this.message = errorCode.getMessage();
    }
}

