package com.example.cs25service.domain.admin.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor
public class UserPageResponseDto {

    private final Long userId;

    private final String email;

    private final String name;

    private final String socialType;

    private final boolean isActive;

    private final String role;
}
