package com.example.cs25.global.dto;

import com.example.cs25.domain.users.exception.UserException;
import com.example.cs25.domain.users.exception.UserExceptionCode;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Arrays;

public enum Role {
    USER,
    ADMIN;

    @JsonCreator
    public static Role forValue(String value) {
        return Arrays.stream(Role.values())
            .filter(v -> v.name().equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(() -> new UserException(UserExceptionCode.INVALID_ROLE));
    }
}

