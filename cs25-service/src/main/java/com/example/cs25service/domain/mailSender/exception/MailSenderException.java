package com.example.cs25service.domain.mailSender.exception;

import org.springframework.http.HttpStatus;

import com.example.cs25common.global.exception.BaseException;

import lombok.Getter;

@Getter
public class MailSenderException extends BaseException {
	private final MailSenderExceptionCode errorCode;
	private final HttpStatus httpStatus;
	private final String message;

	public MailSenderException(MailSenderExceptionCode errorCode) {
		this.errorCode = errorCode;
		this.httpStatus = errorCode.getHttpStatus();
		this.message = errorCode.getMessage();
	}
}