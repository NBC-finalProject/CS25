package com.example.cs25.global.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor
public class ApiResponse<T> {
    private final int httpCode;

    @JsonInclude(JsonInclude.Include.NON_NULL) // null 이면 응답 JSON 에서 생략됨
    private final T data;
}
