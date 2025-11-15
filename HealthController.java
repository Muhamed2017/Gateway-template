package com.enterprise.gateway.controller;

import com.enterprise.gateway.dto.GatewayDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.info.BuildProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;

/**
 * Health check controller for monitoring
 */
@RestController
@RequestMapping("/health")
@Tag(name = "Health", description = "Health check endpoints")
public class HealthController {

    private final DataSource dataSource;
    private final RedisTemplate<String, String> redisTemplate;
    private final BuildProperties buildProperties;

    public HealthController(DataSource dataSource, 
                          @org.springframework.beans.factory.annotation.Autowired(required = false) RedisTemplate<String, String> redisTemplate,
                          @org.springframework.beans.factory.annotation.Autowired(required = false) BuildProperties buildProperties) {
        this.dataSource = dataSource;
        this.redisTemplate = redisTemplate;
        this.buildProperties = buildProperties;
    }

    @GetMapping
    @Operation(summary = "Health check", description = "Comprehensive health status of the application")
    public ResponseEntity<GatewayDto.ApiResponse<GatewayDto.HealthResponse>> health() {
        GatewayDto.HealthResponse.DatabaseHealth dbHealth = checkDatabaseHealth();
        GatewayDto.HealthResponse.CacheHealth cacheHealth = checkCacheHealth();

        String overallStatus = (dbHealth.getStatus().equals("UP") && cacheHealth.getStatus().equals("UP")) 
            ? "UP" : "DOWN";

        GatewayDto.HealthResponse healthResponse = GatewayDto.HealthResponse.builder()
            .status(overallStatus)
            .timestamp(LocalDateTime.now())
            .version(buildProperties != null ? buildProperties.getVersion() : "unknown")
            .database(dbHealth)
            .cache(cacheHealth)
            .build();

        return ResponseEntity.ok(GatewayDto.ApiResponse.success(healthResponse));
    }

    private GatewayDto.HealthResponse.DatabaseHealth checkDatabaseHealth() {
        long startTime = System.currentTimeMillis();
        try (Connection connection = dataSource.getConnection()) {
            boolean isValid = connection.isValid(2);
            long responseTime = System.currentTimeMillis() - startTime;
            
            return GatewayDto.HealthResponse.DatabaseHealth.builder()
                .status(isValid ? "UP" : "DOWN")
                .responseTime(responseTime)
                .build();
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            return GatewayDto.HealthResponse.DatabaseHealth.builder()
                .status("DOWN")
                .responseTime(responseTime)
                .build();
        }
    }

    private GatewayDto.HealthResponse.CacheHealth checkCacheHealth() {
        if (redisTemplate == null) {
            return GatewayDto.HealthResponse.CacheHealth.builder()
                .status("DISABLED")
                .responseTime(0L)
                .build();
        }
        
        long startTime = System.currentTimeMillis();
        try {
            redisTemplate.opsForValue().set("health-check", "OK");
            String value = redisTemplate.opsForValue().get("health-check");
            long responseTime = System.currentTimeMillis() - startTime;
            
            return GatewayDto.HealthResponse.CacheHealth.builder()
                .status("OK".equals(value) ? "UP" : "DOWN")
                .responseTime(responseTime)
                .build();
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            return GatewayDto.HealthResponse.CacheHealth.builder()
                .status("DOWN")
                .responseTime(responseTime)
                .build();
        }
    }
}
