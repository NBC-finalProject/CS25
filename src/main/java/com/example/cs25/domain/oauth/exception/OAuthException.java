package com.example.cs25.domain.oauth.exception;

import org.springframework.http.HttpStatus;

public class OAuthException {
    private final OAuthExceptionCode errorCode;
    private final HttpStatus httpStatus;
    private final String message;

    /**
     * Constructs an OauthException with the specified error code, initializing the associated HTTP status and message.
     *
     * @param errorCode the OAuth exception code containing error details
     */
    public OAuthException(OAuthExceptionCode errorCode) {
        this.errorCode = errorCode;
        this.httpStatus = errorCode.getHttpStatus();
        this.message = errorCode.getMessage();
    }
}
