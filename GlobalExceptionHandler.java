package com.enterprise.gateway.exception;

import com.enterprise.gateway.dto.GatewayDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Global exception handler for all API Gateway exceptions
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(GatewayException.class)
    public ResponseEntity<GatewayDto.ApiResponse<Void>> handleGatewayException(
            GatewayException ex, WebRequest request) {
        String requestId = UUID.randomUUID().toString();
        
        log.error("Gateway exception occurred. RequestId: {}, Code: {}, Message: {}", 
            requestId, ex.getCode(), ex.getMessage(), ex);

        GatewayDto.ErrorDetails errorDetails = GatewayDto.ErrorDetails.builder()
            .code(ex.getCode())
            .message(ex.getMessage())
            .build();

        GatewayDto.ApiResponse<Void> response = GatewayDto.ApiResponse.<Void>builder()
            .success(false)
            .message(ex.getMessage())
            .error(errorDetails)
            .requestId(requestId)
            .timestamp(LocalDateTime.now())
            .build();

        return new ResponseEntity<>(response, ex.getStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GatewayDto.ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {
        String requestId = UUID.randomUUID().toString();
        
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "Validation failed";
        
        log.error("Validation exception occurred. RequestId: {}, Message: {}", requestId, message);

        GatewayDto.ErrorDetails errorDetails = GatewayDto.ErrorDetails.builder()
            .code("VALIDATION_ERROR")
            .message(message)
            .field(fieldError != null ? fieldError.getField() : null)
            .rejectedValue(fieldError != null ? fieldError.getRejectedValue() : null)
            .build();

        GatewayDto.ApiResponse<Void> response = GatewayDto.ApiResponse.<Void>builder()
            .success(false)
            .message("Validation failed")
            .error(errorDetails)
            .requestId(requestId)
            .timestamp(LocalDateTime.now())
            .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<GatewayDto.ApiResponse<Void>> handleAuthenticationException(
            AuthenticationException ex, WebRequest request) {
        String requestId = UUID.randomUUID().toString();
        
        log.error("Authentication exception occurred. RequestId: {}, Message: {}", 
            requestId, ex.getMessage());

        GatewayDto.ErrorDetails errorDetails = GatewayDto.ErrorDetails.builder()
            .code("AUTHENTICATION_FAILED")
            .message(ex.getMessage())
            .build();

        GatewayDto.ApiResponse<Void> response = GatewayDto.ApiResponse.<Void>builder()
            .success(false)
            .message("Authentication failed")
            .error(errorDetails)
            .requestId(requestId)
            .timestamp(LocalDateTime.now())
            .build();

        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<GatewayDto.ApiResponse<Void>> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {
        String requestId = UUID.randomUUID().toString();
        
        log.error("Access denied exception occurred. RequestId: {}, Message: {}", 
            requestId, ex.getMessage());

        GatewayDto.ErrorDetails errorDetails = GatewayDto.ErrorDetails.builder()
            .code("ACCESS_DENIED")
            .message(ex.getMessage())
            .build();

        GatewayDto.ApiResponse<Void> response = GatewayDto.ApiResponse.<Void>builder()
            .success(false)
            .message("Access denied")
            .error(errorDetails)
            .requestId(requestId)
            .timestamp(LocalDateTime.now())
            .build();

        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GatewayDto.ApiResponse<Void>> handleGenericException(
            Exception ex, WebRequest request) {
        String requestId = UUID.randomUUID().toString();
        
        log.error("Unexpected exception occurred. RequestId: {}, Message: {}", 
            requestId, ex.getMessage(), ex);

        GatewayDto.ErrorDetails errorDetails = GatewayDto.ErrorDetails.builder()
            .code("INTERNAL_ERROR")
            .message("An unexpected error occurred")
            .build();

        GatewayDto.ApiResponse<Void> response = GatewayDto.ApiResponse.<Void>builder()
            .success(false)
            .message("Internal server error")
            .error(errorDetails)
            .requestId(requestId)
            .timestamp(LocalDateTime.now())
            .build();

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
