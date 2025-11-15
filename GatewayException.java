package com.enterprise.gateway.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Custom exceptions for API Gateway
 */
@Getter
public class GatewayException extends RuntimeException {
    private final HttpStatus status;
    private final String code;

    public GatewayException(String message, HttpStatus status, String code) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public GatewayException(String message, Throwable cause, HttpStatus status, String code) {
        super(message, cause);
        this.status = status;
        this.code = code;
    }
}
