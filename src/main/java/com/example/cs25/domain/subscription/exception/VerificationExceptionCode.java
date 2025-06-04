package com.example.cs25.domain.subscription.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum VerificationExceptionCode {

    VERIFICATION_CODE_MISMATCH_ERROR(false, HttpStatus.BAD_REQUEST, "인증코드가 일치하지 않습니다."),
    VERIFICATION_CODE_EXPIRED_ERROR(false, HttpStatus.GONE, "인증코드가 만료되었습니다. 다시 요청해주세요.");
    private final boolean isSuccess;
    private final HttpStatus httpStatus;
    private final String message;
}
