package com.example.cs25.global.jwt.dto;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class JwtErrorResponse {
    private final boolean success;
    private final int status;
    private final String message;
}
