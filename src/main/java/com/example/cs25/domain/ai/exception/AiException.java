package com.example.cs25.domain.ai.exception;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AiException extends RuntimeException{
    private final AiExceptionCode errorCode;
    private final HttpStatus httpStatus;
    private final String message;

    /**
     * Constructs an AiException using the specified error code.
     *
     * Initializes the exception's HTTP status and message based on the provided AiExceptionCode.
     *
     * @param errorCode the AI exception code containing error details
     */
    public AiException(AiExceptionCode errorCode) {
        this.errorCode = errorCode;
        this.httpStatus = errorCode.getHttpStatus();
        this.message = errorCode.getMessage();
    }
}
