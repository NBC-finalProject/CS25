package com.example.cs25common.global.exception;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles exceptions of type {@code BaseException} and returns a structured error response.
     *
     * @param ex the exception containing the HTTP status and error message
     * @return a {@code ResponseEntity} with a JSON body describing the error and the appropriate
     * HTTP status
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<Map<String, Object>> handleServerException(BaseException ex) {
        HttpStatus status = ex.getHttpStatus();
        return getErrorResponse(status, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class) //@Valid 오류 핸들링
    public ResponseEntity<Map<String, Object>> handleValidationException(
        MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .collect(Collectors.joining(", "));
        return getErrorResponse(HttpStatus.BAD_REQUEST, errorMessage);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class) //Json 파싱 오류 핸들링
    public ResponseEntity<Map<String, Object>> handleJsonParseError(
        HttpMessageNotReadableException ex) {
        String message = "JSON 파싱 에러 " + ex.getMostSpecificCause().getMessage();
        return getErrorResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
        IllegalArgumentException e) {
        String message = "유효하지 않은 요청: " + e.getMessage();
        return getErrorResponse(HttpStatus.BAD_REQUEST, message);
    }

    public ResponseEntity<Map<String, Object>> getErrorResponse(HttpStatus status, String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", status.name());
        errorResponse.put("code", status.value());
        errorResponse.put("message", message);
        return new ResponseEntity<>(errorResponse, status);
    }
}