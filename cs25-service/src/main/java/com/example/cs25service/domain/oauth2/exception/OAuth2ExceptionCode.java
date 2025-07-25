package com.example.cs25service.domain.oauth2.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OAuth2ExceptionCode {

    SOCIAL_PROVIDER_UNSUPPORTED(false, HttpStatus.BAD_REQUEST, "지원하지 않는 소셜 로그인 기능입니다."),
    SOCIAL_PROVIDER_NOT_FOUND(false, HttpStatus.NOT_FOUND, "찾을 수 없는 소셜 로그인 기능입니다."),

    SOCIAL_REQUIRED_FIELDS_MISSING(false, HttpStatus.BAD_REQUEST, "로그인에 필요한 정보가 누락되었습니다."),
    SOCIAL_EMAIL_NOT_FOUND(false, HttpStatus.BAD_REQUEST, "이메일 정보를 가져오지 못하였습니다."),
    SOCIAL_EMAIL_NOT_FOUND_WITH_TOKEN(false, HttpStatus.BAD_REQUEST, "액세스 토큰을 사용했지만 이메일 정보를 찾을 수 없습니다."),
    SOCIAL_NAME_NOT_FOUND(false, HttpStatus.BAD_REQUEST, "이름(닉네임) 정보를 가져오지 못하였습니다."),
    SOCIAL_ATTRIBUTES_PARSING_FAILED(false, HttpStatus.BAD_REQUEST, "소셜에서 데이터를 제대로 파싱하지 못하였습니다.");


    private final boolean isSuccess;
    private final HttpStatus httpStatus;
    private final String message;
}

