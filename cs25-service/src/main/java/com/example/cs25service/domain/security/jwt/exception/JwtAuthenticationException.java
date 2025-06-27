package com.example.cs25service.domain.security.jwt.exception;

import com.example.cs25common.global.exception.BaseException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class JwtAuthenticationException extends BaseException {

    private final JwtExceptionCode errorCode;
    private final HttpStatus httpStatus;
    private final String message;

    /**
     * Constructs a new QuizException with the specified error code.
     * <p>
     * Initializes the exception with the provided QuizExceptionCode, setting the corresponding HTTP
     * status and error message.
     *
     * @param errorCode the quiz-specific error code containing HTTP status and message details
     */
    public JwtAuthenticationException(JwtExceptionCode errorCode) {
        this.errorCode = errorCode;
        this.httpStatus = errorCode.getHttpStatus();
        this.message = errorCode.getMessage();
    }
}
