package com.example.cs25entity.domain.subscription.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DayOfWeek {
    SUNDAY(0),
    MONDAY(1),
    TUESDAY(2),
    WEDNESDAY(3),
    THURSDAY(4),
    FRIDAY(5),
    SATURDAY(6);

    private final int bitIndex;

    public int getBitValue() {
        return 1 << bitIndex;
    }

    public static boolean contains(int bits, DayOfWeek day) {
        return (bits & day.getBitValue()) != 0;
    }
}
