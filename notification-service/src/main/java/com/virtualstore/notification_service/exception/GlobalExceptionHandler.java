package com.virtualstore.notification_service.exception;

import com.virtualstore.notification_service.dto.NotificationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Bean validation failures (missing required fields) */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<NotificationResponse> handleValidation(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));

        return ResponseEntity.badRequest().body(
                NotificationResponse.builder()
                        .success(false)
                        .message("Validation failed — " + errors)
                        .build());
    }

    /** Business-level validation (missing phone / email) */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<NotificationResponse> handleIllegalArg(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(
                NotificationResponse.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .build());
    }

    /** Catch-all */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<NotificationResponse> handleGeneral(Exception ex) {
        log.error("[ExceptionHandler] Unhandled exception: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                NotificationResponse.builder()
                        .success(false)
                        .message("Internal error — please try again later")
                        .build());
    }
}