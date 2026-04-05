package com.virtualstore.user_service.exception;

import org.springframework.http.HttpStatus;

public class EmailAlreadyExistsException extends AppException {
    public EmailAlreadyExistsException(String email) {
        super("An account with email '" + email + "' already exists.", HttpStatus.CONFLICT);
    }
}