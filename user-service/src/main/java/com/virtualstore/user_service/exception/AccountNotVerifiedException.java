package com.virtualstore.user_service.exception;

import org.springframework.http.HttpStatus;

public class AccountNotVerifiedException extends AppException {
    public AccountNotVerifiedException() {
        super("Email address not verified. Please check your inbox.", HttpStatus.FORBIDDEN);
    }
}