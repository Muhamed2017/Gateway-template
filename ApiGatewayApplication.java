package com.enterprise.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Enterprise API Gateway Application
 * 
 * Entry point for the API Gateway that provides:
 * - Unified authentication and authorization
 * - Request routing to downstream services
 * - Comprehensive audit logging
 * - Rate limiting and circuit breaking
 * - Centralized security enforcement
 */
@SpringBootApplication
@EnableCaching
@EnableJpaAuditing
@EnableAsync
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
