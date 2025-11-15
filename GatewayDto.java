package com.enterprise.gateway.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTOs for API Gateway operations
 */
public class GatewayDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LoginRequest {
        @NotBlank(message = "API key is required")
        private String apiKey;

        @NotBlank(message = "Secret is required")
        private String secret;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class LoginResponse {
        private String token;
        private String tokenType;
        private Long expiresIn;
        private String refreshToken;
        private PartnerInfo partnerInfo;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PartnerInfo {
        private Long id;
        private String name;
        private String email;
        private Set<String> roles;
        private String status;
        private LocalDateTime lastLogin;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RegisterPartnerRequest {
        @NotBlank(message = "Partner name is required")
        @Size(min = 3, max = 100)
        private String name;

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Secret is required")
        @Size(min = 8, max = 100)
        private String secret;

        private Set<String> roles;
        
        private Set<String> allowedIpAddresses;
        
        private Integer rateLimitPerMinute;
        
        private Integer rateLimitPerHour;
        
        private String description;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PartnerResponse {
        private Long id;
        private String name;
        private String email;
        private String apiKey;
        private String status;
        private Set<String> roles;
        private Set<String> allowedIpAddresses;
        private Integer rateLimitPerMinute;
        private Integer rateLimitPerHour;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private String description;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;
        private String requestId;
        private LocalDateTime timestamp;
        private ErrorDetails error;

        public static <T> ApiResponse<T> success(T data) {
            return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
        }

        public static <T> ApiResponse<T> success(String message, T data) {
            return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
        }

        public static <T> ApiResponse<T> error(String message) {
            return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
        }

        public static <T> ApiResponse<T> error(String message, ErrorDetails error) {
            return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .error(error)
                .timestamp(LocalDateTime.now())
                .build();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ErrorDetails {
        private String code;
        private String message;
        private String field;
        private Object rejectedValue;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuditLogResponse {
        private Long id;
        private String partnerName;
        private String requestId;
        private String method;
        private String endpoint;
        private Integer httpStatus;
        private String clientIp;
        private Long durationMs;
        private LocalDateTime timestamp;
        private String downstreamService;
        private Boolean cacheHit;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HealthResponse {
        private String status;
        private LocalDateTime timestamp;
        private String version;
        private DatabaseHealth database;
        private CacheHealth cache;
        
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class DatabaseHealth {
            private String status;
            private Long responseTime;
        }
        
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class CacheHealth {
            private String status;
            private Long responseTime;
        }
    }
}
