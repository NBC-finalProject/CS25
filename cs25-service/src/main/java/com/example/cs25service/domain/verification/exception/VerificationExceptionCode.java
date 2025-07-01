package com.example.cs25service.domain.verification.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum VerificationExceptionCode {

    VERIFICATION_CODE_MISMATCH_ERROR(false, HttpStatus.BAD_REQUEST, "인증코드가 일치하지 않습니다."),
    VERIFICATION_CODE_EXPIRED_ERROR(false, HttpStatus.GONE, "인증코드가 만료되었습니다. 다시 요청해주세요."),
    TOO_MANY_ATTEMPTS_ERROR(false, HttpStatus.TOO_MANY_REQUESTS, "최대 요청 횟수를 초과하였습니다. 나중에 다시 시도해주세요"),
    TOO_MANY_REQUESTS_DAILY(false, HttpStatus.TOO_MANY_REQUESTS, "최대 발급 횟수를 초과하였습니다. 24시간 후에 다시 시도해주세요");
    private final boolean isSuccess;
    private final HttpStatus httpStatus;
    private final String message;
}
