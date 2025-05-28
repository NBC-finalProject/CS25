package com.example.cs25.domain.userQuizAnswer.exception;

import com.example.cs25.global.exception.BaseException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class UserQuizAnswerException extends BaseException {
  private final UserQuizAnswerExceptionCode errorCode;
  private final HttpStatus httpStatus;
  private final String message;

  public UserQuizAnswerException(UserQuizAnswerExceptionCode errorCode) {
    this.errorCode = errorCode;
    this.httpStatus = errorCode.getHttpStatus();
    this.message = errorCode.getMessage();
  }
}

