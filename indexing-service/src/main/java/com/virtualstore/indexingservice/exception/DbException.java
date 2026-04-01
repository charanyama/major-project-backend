package com.virtualstore.indexingservice.exception;

public class DbException extends RuntimeException {

    private final String errorCode;

    public DbException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public DbException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}