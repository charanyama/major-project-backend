package com.virtualstore.gateway.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

/**
 * GlobalExceptionHandler
 *
 * Custom reactive error handler for Spring Cloud Gateway.
 *
 * Intercepts all unhandled exceptions and formats them as consistent JSON responses.
 * Specifically handles:
 *   - 401 Unauthorized (invalid/missing JWT)
 *   - 403 Forbidden (insufficient roles)
 *   - 500 Internal Server Error (unexpected failures)
 *
 * Ordering: -2 ensures this runs before Spring Boot's DefaultErrorWebExceptionHandler (-1).
 */
@Slf4j
@Component
@Order(-2)
public class GlobalExceptionHandler extends AbstractErrorWebExceptionHandler {

    public GlobalExceptionHandler(
            ErrorAttributes errorAttributes,
            WebProperties webProperties,
            ApplicationContext applicationContext,
            ServerCodecConfigurer codecConfigurer) {

        super(errorAttributes, webProperties.getResources(), applicationContext);
        super.setMessageWriters(codecConfigurer.getWriters());
        super.setMessageReaders(codecConfigurer.getReaders());
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    /**
     * Renders a JSON error response for any unhandled exception.
     * Determines the HTTP status code based on exception type.
     */
    private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
        Throwable error = getError(request);

        HttpStatus status = determineStatus(error);

        String message = determineMessage(error, status);

        log.error("Gateway error [{}]: {} path={}",
                  status.value(), error.getMessage(), request.path());

        Map<String, Object> errorBody = Map.of(
            "timestamp", Instant.now().toString(),
            "status",    status.value(),
            "error",     status.getReasonPhrase(),
            "message",   message,
            "path",      request.path()
        );

        return ServerResponse
            .status(status)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(errorBody));
    }

    private HttpStatus determineStatus(Throwable error) {
        if (error instanceof OAuth2AuthenticationException) {
            return HttpStatus.UNAUTHORIZED;
        }
        if (error instanceof AccessDeniedException) {
            return HttpStatus.FORBIDDEN;
        }
        if (error instanceof org.springframework.web.server.ResponseStatusException rse) {
            return HttpStatus.valueOf(rse.getStatusCode().value());
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String determineMessage(Throwable error, HttpStatus status) {
        return switch (status) {
            case UNAUTHORIZED -> "Authentication required. Provide a valid Bearer token.";
            case FORBIDDEN    -> "You do not have permission to access this resource.";
            default           -> "An unexpected error occurred. Please try again later.";
        };
    }
}