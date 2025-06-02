package com.example.cs25.global.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApiResponse<T> {
    private final int httpCode;
    private final T data;

    public ApiResponse(int httpCode, T data) {
        this.httpCode = httpCode;
        this.data = data;
    }
}
