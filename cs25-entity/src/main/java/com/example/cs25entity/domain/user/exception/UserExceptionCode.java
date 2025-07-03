package com.example.cs25entity.domain.user.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserExceptionCode {
    EMAIL_DUPLICATION(false, HttpStatus.CONFLICT, "해당 이메일로 구독을 사용중입니다. 다른 소셜 로그인을 사용해주세요."),
    EVENT_CRUD_FAILED(false, HttpStatus.INTERNAL_SERVER_ERROR, "이벤트 값을 레디스에 읽기/저장 실패했으요"),
    LOCK_FAILED(false, HttpStatus.CONFLICT, "요청 시간 초과, 락 획득 실패"),
    INVALID_ROLE(false, HttpStatus.BAD_REQUEST, "역할 값이 잘못되었습니다."),
    UNAUTHORIZED_ROLE(false, HttpStatus.FORBIDDEN, "권한이 없습니다."),
    TOKEN_NOT_MATCHED(false, HttpStatus.BAD_REQUEST, "유효한 리프레시 토큰 값이 아닙니다."),
    NOT_FOUND_USER(false, HttpStatus.NOT_FOUND, "해당 유저를 찾을 수 없습니다."),
    NOT_FOUND_SUBSCRIPTION(false, HttpStatus.NOT_FOUND, "해당 유저에게 구독 정보가 없습니다."),
    DUPLICATE_SUBSCRIPTION_ERROR(false, HttpStatus.CONFLICT, "이미 구독 정보가 있는 사용자입니다."),
    INACTIVE_USER(false, HttpStatus.BAD_REQUEST, "이미 삭제된 유저입니다.");

    private final boolean isSuccess;
    private final HttpStatus httpStatus;
    private final String message;
}
