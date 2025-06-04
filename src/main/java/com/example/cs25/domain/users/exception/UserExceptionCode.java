package com.example.cs25.domain.users.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;


@Getter
@RequiredArgsConstructor
public enum UserExceptionCode {
    EMAIL_DUPLICATION(false, HttpStatus.CONFLICT, "이미 사용중인 이메일입니다."),
    EVENT_CRUD_FAILED(false, HttpStatus.INTERNAL_SERVER_ERROR, "이벤트 값을 레디스에 읽기/저장 실패했으요"),
    LOCK_FAILED(false, HttpStatus.CONFLICT, "요청 시간 초과, 락 획득 실패"),
    INVALID_ROLE(false, HttpStatus.BAD_REQUEST, "역할 값이 잘못되었습니다."),
    NOT_FOUND_USER(false, HttpStatus.NOT_FOUND, "해당 유저를 찾을 수 없습니다."),

    UNSUPPORTED_SOCIAL_PROVIDER(false, HttpStatus.BAD_REQUEST, "지원하지 않는 소셜 로그인 기능입니다."),
    OAUTH2_PROFILE_INCOMPLETE(false, HttpStatus.BAD_REQUEST, "해당 사용자 정보가 없습니다."),
    KAKAO_PROFILE_INCOMPLETE(false, HttpStatus.BAD_REQUEST, "해당 사용자 정보가 없습니다."),
    GITHUB_PROFILE_INCOMPLETE(false, HttpStatus.BAD_REQUEST, "해당 사용자 정보가 없습니다.");

    private final boolean isSuccess;
    private final HttpStatus httpStatus;
    private final String message;
}
