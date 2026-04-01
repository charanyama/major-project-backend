package com.virtualstore.indexingservice.exception;

public class ExternalApiException extends DbException {

    public ExternalApiException(String message) {
        super(message, "EXTERNAL_API_ERROR");
    }

    public ExternalApiException(String message, Throwable cause) {
        super(message, "EXTERNAL_API_ERROR", cause);
    }
}