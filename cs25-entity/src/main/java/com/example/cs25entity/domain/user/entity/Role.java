package com.example.cs25entity.domain.user.entity;

import com.example.cs25entity.domain.user.exception.UserException;
import com.example.cs25entity.domain.user.exception.UserExceptionCode;
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

