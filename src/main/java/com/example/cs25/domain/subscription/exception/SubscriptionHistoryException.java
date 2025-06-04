package com.example.cs25.domain.subscription.exception;

import org.springframework.http.HttpStatus;

import com.example.cs25.global.exception.BaseException;

import lombok.Getter;

@Getter
public class SubscriptionHistoryException extends BaseException {
	private final SubscriptionHistoryExceptionCode errorCode;
	private final HttpStatus httpStatus;
	private final String message;

	public SubscriptionHistoryException(SubscriptionHistoryExceptionCode errorCode) {
		this.errorCode = errorCode;
		this.httpStatus = errorCode.getHttpStatus();
		this.message = errorCode.getMessage();
	}
}
