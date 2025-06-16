package com.example.cs25common.global.domain.mail.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MailExceptionCode {

    EMAIL_SEND_FAILED_ERROR(false, HttpStatus.INTERNAL_SERVER_ERROR, "이메일 발송에 실패했습니다."),
    EMAIL_NOT_FOUND_ERROR(false, HttpStatus.NOT_FOUND, "해당 이메일를 찾을 수 없습니다"),
    VERIFICATION_CODE_NOT_FOUND_ERROR(false, HttpStatus.NOT_FOUND, "해당 이메일에 대한 인증 요청이 존재하지 않습니다."),
    EMAIL_BAD_REQUEST_ERROR(false, HttpStatus.BAD_REQUEST, "이메일 주소가 올바르지 않습니다."),
    VERIFICATION_CODE_BAD_REQUEST_ERROR(false, HttpStatus.BAD_REQUEST, "인증코드가 올바르지 않습니다."),
    VERIFICATION_GONE_ERROR(false, HttpStatus.GONE, "인증 코드가 만료되었습니다. 다시 요청해주세요.");
    private final boolean isSuccess;
    private final HttpStatus httpStatus;
    private final String message;
}
