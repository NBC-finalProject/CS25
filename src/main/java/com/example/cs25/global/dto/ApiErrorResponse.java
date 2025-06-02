package com.example.cs25.global.dto;

import lombok.Getter;

@Getter
public class ApiErrorResponse {

    private final int httpCode;
    private final String message;

    public ApiErrorResponse(int httpCode, String message) {
        this.httpCode = httpCode;
        this.message = message;
    }
}
