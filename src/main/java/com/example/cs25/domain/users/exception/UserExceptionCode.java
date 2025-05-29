package com.example.cs25.domain.users.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;


@Getter
@RequiredArgsConstructor
public enum UserExceptionCode {
    //예시임
    UNSUPPORTED_SOCIAL_PROVIDER(false, HttpStatus.BAD_REQUEST, "지원하지 않는 소셜 로그인 기능입니다."),
    EMAIL_DUPLICATION(false, HttpStatus.CONFLICT, "이미 사용중인 이메일입니다."),
    EVENT_CRUD_FAILED(false, HttpStatus.INTERNAL_SERVER_ERROR, "이벤트 값을 레디스에 읽기/저장 실패했으요"),
    LOCK_FAILED(false, HttpStatus.CONFLICT, "요청 시간 초과, 락 획득 실패"),
    INVALID_EVENT(false, HttpStatus.BAD_REQUEST, "지금은 이벤트에 참여할 수 없어요"),
    INVALID_ROLE(false, HttpStatus.BAD_REQUEST, "역할 값이 잘못되었습니다." );

    private final boolean isSuccess;
    private final HttpStatus httpStatus;
    private final String message;
}
