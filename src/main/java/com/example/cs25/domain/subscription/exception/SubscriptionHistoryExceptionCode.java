package com.example.cs25.domain.subscription.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SubscriptionHistoryExceptionCode {
	NOT_FOUND_SUBSCRIPTION_HISTORY_ERROR(false, HttpStatus.NOT_FOUND, "존재하지 않는 구독 내역입니다.");

	private final boolean isSuccess;
	private final HttpStatus httpStatus;
	private final String message;
}
