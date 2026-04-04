package com.virtualstore.orderservice.dto;

import java.time.Instant;

import org.springframework.http.HttpStatus;

import lombok.Data;

@Data
public class ErrorResponse {
    private HttpStatus status;
    private String message;
    private String errorCode;
    private Instant timestamp;

    public ErrorResponse(HttpStatus notFound, String message, String errorCode) {
        this.status = notFound;
        this.message = message;
        this.errorCode = errorCode;
        this.timestamp = Instant.now();
    }

}
