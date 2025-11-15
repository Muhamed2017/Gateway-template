package com.enterprise.gateway.exception;

import org.springframework.http.HttpStatus;

/**
 * Specific exception types for different error scenarios
 */
public class Exceptions {

    public static class AuthenticationException extends GatewayException {
        public AuthenticationException(String message) {
            super(message, HttpStatus.UNAUTHORIZED, "AUTH_ERROR");
        }
    }

    public static class AuthorizationException extends GatewayException {
        public AuthorizationException(String message) {
            super(message, HttpStatus.FORBIDDEN, "AUTHZ_ERROR");
        }
    }

    public static class PartnerNotFoundException extends GatewayException {
        public PartnerNotFoundException(String message) {
            super(message, HttpStatus.NOT_FOUND, "PARTNER_NOT_FOUND");
        }
    }

    public static class InvalidApiKeyException extends GatewayException {
        public InvalidApiKeyException(String message) {
            super(message, HttpStatus.UNAUTHORIZED, "INVALID_API_KEY");
        }
    }

    public static class RateLimitExceededException extends GatewayException {
        public RateLimitExceededException(String message) {
            super(message, HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMIT_EXCEEDED");
        }
    }

    public static class IpAddressNotAllowedException extends GatewayException {
        public IpAddressNotAllowedException(String message) {
            super(message, HttpStatus.FORBIDDEN, "IP_NOT_ALLOWED");
        }
    }

    public static class PartnerSuspendedException extends GatewayException {
        public PartnerSuspendedException(String message) {
            super(message, HttpStatus.FORBIDDEN, "PARTNER_SUSPENDED");
        }
    }

    public static class InvalidTokenException extends GatewayException {
        public InvalidTokenException(String message) {
            super(message, HttpStatus.UNAUTHORIZED, "INVALID_TOKEN");
        }
    }

    public static class ServiceUnavailableException extends GatewayException {
        public ServiceUnavailableException(String message) {
            super(message, HttpStatus.SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE");
        }
    }

    public static class BadRequestException extends GatewayException {
        public BadRequestException(String message) {
            super(message, HttpStatus.BAD_REQUEST, "BAD_REQUEST");
        }
    }

    public static class DuplicatePartnerException extends GatewayException {
        public DuplicatePartnerException(String message) {
            super(message, HttpStatus.CONFLICT, "DUPLICATE_PARTNER");
        }
    }
}
