package com.example.cs25.domain.userQuizAnswer.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserQuizAnswerExceptionCode {
    //예시임
    NOT_FOUND_EVENT(false, HttpStatus.NOT_FOUND, "해당 이벤트를 찾을 수 없습니다"),
    EVENT_OUT_OF_STOCK(false, HttpStatus.GONE, "당첨자가 모두 나왔습니다. 다음 기회에 다시 참여해주세요"),
    EVENT_CRUD_FAILED(false, HttpStatus.INTERNAL_SERVER_ERROR, "이벤트 값을 레디스에 읽기/저장 실패했으요"),
    LOCK_FAILED(false, HttpStatus.CONFLICT, "요청 시간 초과, 락 획득 실패"),
    INVALID_EVENT(false, HttpStatus.BAD_REQUEST, "지금은 이벤트에 참여할 수 없어요"),
    DUPLICATED_EVENT_ID(false, HttpStatus.BAD_REQUEST, "중복되는 이벤트 ID 입니다." );

    private final boolean isSuccess;
    private final HttpStatus httpStatus;
    private final String message;
}
