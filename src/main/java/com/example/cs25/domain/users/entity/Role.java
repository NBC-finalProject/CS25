package com.example.cs25.domain.users.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Arrays;

public enum Role {
    ROLE_USER,
    ROLE_ADMIN;

    @JsonCreator
    public static Role forValue(String value) {
        return Arrays.stream(Role.values())
            .filter(v -> v.name().equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(() -> new UserException(UserExceptionCode.INVALID_ROLE));
    }
}

