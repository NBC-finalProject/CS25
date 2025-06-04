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
	ONE_MONTH(30),
	THREE_MONTHS(90),
	SIX_MONTHS(180),
	ONE_YEAR(365);

	private final int days;

	/**
	 * JSON → SubscriptionPeriod 역직렬화 작업을 도와주는 메서드
	 * @param days 구독기간
	 * @return SubscriptionPeriod Enum 객체를 반환
	 */
	@JsonCreator
	public static SubscriptionPeriod from(int days) {
		for (SubscriptionPeriod period : values()) {
			if (period.days == days) {
				return period;
			}
		}
		throw new SubscriptionException(SubscriptionExceptionCode.ILLEGAL_SUBSCRIPTION_PERIOD_ERROR);
	}
}
