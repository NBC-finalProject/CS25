package com.example.cs25service.domain.security.jwt.dto;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class JwtErrorResponse {

    private final boolean success;
    private final int status;
    private final String message;
}
