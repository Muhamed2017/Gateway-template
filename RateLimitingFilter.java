package com.enterprise.gateway.filter;

import com.enterprise.gateway.exception.Exceptions;
import com.enterprise.gateway.model.Partner;
import com.enterprise.gateway.service.AuditService;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting filter for partner requests
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RateLimiterRegistry rateLimiterRegistry = RateLimiterRegistry.ofDefaults();
    private final ConcurrentHashMap<Long, RateLimiter> partnerRateLimiters = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof Partner partner) {
            RateLimiter rateLimiter = getRateLimiterForPartner(partner);
            
            boolean permitted = rateLimiter.acquirePermission();
            
            if (!permitted) {
                log.warn("Rate limit exceeded for partner: {}", partner.getName());
                throw new Exceptions.RateLimitExceededException(
                    "Rate limit exceeded. Please try again later.");
            }
        }

        filterChain.doFilter(request, response);
    }

    private RateLimiter getRateLimiterForPartner(Partner partner) {
        return partnerRateLimiters.computeIfAbsent(partner.getId(), id -> {
            int limitPerMinute = partner.getRateLimitPerMinute() != null ? 
                partner.getRateLimitPerMinute() : 60;
            
            RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(limitPerMinute)
                .limitRefreshPeriod(Duration.ofMinutes(1))
                .timeoutDuration(Duration.ofSeconds(1))
                .build();
            
            return rateLimiterRegistry.rateLimiter("partner-" + id, config);
        });
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/v1/auth/login") || 
               path.startsWith("/api/v1/auth/register") ||
               path.startsWith("/actuator") ||
               path.startsWith("/health");
    }
}
