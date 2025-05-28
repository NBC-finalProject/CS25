package com.example.cs25.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles exceptions of type {@code BaseException} and returns a structured error response.
     *
     * @param ex the exception containing the HTTP status and error message
     * @return a {@code ResponseEntity} with a JSON body describing the error and the appropriate HTTP status
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<Map<String, Object>> handleServerException(BaseException ex) {
        HttpStatus status = ex.getHttpStatus();
        return getErrorResponse(status, ex.getMessage());
    }

    /**
     * Constructs a structured error response containing the HTTP status, status code, and an error message.
     *
     * @param status the HTTP status to include in the response
     * @param message the error message to include in the response
     * @return a ResponseEntity containing a map with error details and the specified HTTP status
     */
    public ResponseEntity<Map<String, Object>> getErrorResponse(HttpStatus status, String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", status.name());
        errorResponse.put("code", status.value());
        errorResponse.put("message", message);

        return new ResponseEntity<>(errorResponse, status);
    }
}