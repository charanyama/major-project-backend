package com.virtualstore.user_service.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown on wrong password — deliberately vague message to prevent user
 * enumeration
 */
public class InvalidCredentialsException extends AppException {
    public InvalidCredentialsException() {
        super("Invalid email or password.", HttpStatus.UNAUTHORIZED);
    }
}