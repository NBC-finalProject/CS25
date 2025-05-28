package com.example.cs25.global.exception;

import org.springframework.http.HttpStatus;

public abstract class BaseException extends RuntimeException {
    /****
 * Returns the error code associated with this exception.
 *
 * @return an enum value representing the specific error code
 */
public abstract Enum<?> getErrorCode();

    /****
 * Returns the HTTP status code associated with this exception.
 *
 * @return the corresponding HttpStatus for this exception
 */
public abstract HttpStatus getHttpStatus();

    /****
 * Returns a descriptive message explaining the reason for the exception.
 *
 * @return the exception message
 */
public abstract String getMessage();
}
