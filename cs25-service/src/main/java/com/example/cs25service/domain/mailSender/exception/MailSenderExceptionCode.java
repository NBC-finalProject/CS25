package com.example.cs25service.domain.mailSender.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MailSenderExceptionCode {
	NOT_FOUND_STRATEGY(false, HttpStatus.BAD_REQUEST, "메일 전략이 존재하지 않습니다.");

	private final boolean isSuccess;
	private final HttpStatus httpStatus;
	private final String message;
}
