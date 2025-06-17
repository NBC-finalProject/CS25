package com.example.cs25common.global.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ApiResponse<T> {

    private final int httpCode;

    @JsonInclude(JsonInclude.Include.NON_NULL) // null 이면 응답 JSON 에서 생략됨
    private final T data;

    /**
     * 반환할 데이터가 없는 경우 사용되는 생성자
     *
     * @param httpCode httpCode
     */
    public ApiResponse(int httpCode) {
        this.httpCode = httpCode;
        this.data = null;
    }
}
