package com.example.cs25service.domain.security.jwt.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReissueRequestDto {

    private String refreshToken;

    public ReissueRequestDto(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}