package com.virtualstore.user_service.exception;

import org.springframework.http.HttpStatus;
import lombok.Getter;

/**
 * AppException
 *
 * Base runtime exception for all user-service domain errors.
 * Carries an HttpStatus so the GlobalExceptionHandler can map it
 * directly to the correct HTTP response code without a big if/else chain.
 */
@Getter
public class AppException extends RuntimeException {

    private final HttpStatus status;

    public AppException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}