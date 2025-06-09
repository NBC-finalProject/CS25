package com.example.cs25.domain.subscription.entity;

import com.example.cs25.domain.subscription.exception.SubscriptionException;
import com.example.cs25.domain.subscription.exception.SubscriptionExceptionCode;
import com.fasterxml.jackson.annotation.JsonCreator;
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

    private final int months;

    /**
     * JSON → SubscriptionPeriod 역직렬화 작업을 도와주는 메서드
     *
     * @param months 구독개월
     * @return SubscriptionPeriod Enum 객체를 반환
     */
    @JsonCreator
    public static SubscriptionPeriod from(int months) {
        try {
            //int months = Integer.parseInt(value);
            for (SubscriptionPeriod period : values()) {
                if (period.months == months) {
                    return period;
                }
            }
        } catch (NumberFormatException e) {
            // 무시하고 아래 예외로 이동
        }
        throw new SubscriptionException(
            SubscriptionExceptionCode.ILLEGAL_SUBSCRIPTION_PERIOD_ERROR);
    }
}
