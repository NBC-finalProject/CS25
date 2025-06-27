package com.example.cs25entity.domain.subscription.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SubscriptionPeriod {
    NO_PERIOD(0),
    ONE_MONTH(1),
    THREE_MONTHS(3),
    SIX_MONTHS(6),
    ONE_YEAR(12);

    private final long months;

    @JsonValue
    public long getMonths() {
        return months;
    }

    @JsonCreator
    public static SubscriptionPeriod fromMonths(long months) {
        for (SubscriptionPeriod period : values()) {
            if (period.months == months) {
                return period;
            }
        }
        throw new IllegalArgumentException("지원하지 않는 구독개월입니다.: " + months);
    }
}
