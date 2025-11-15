package com.enterprise.gateway.filter;

import com.enterprise.gateway.exception.Exceptions;
import com.enterprise.gateway.model.Partner;
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
import java.util.Set;

/**
 * IP address validation filter
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class IpValidationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof Partner partner) {
            Set<String> allowedIps = partner.getAllowedIpAddresses();
            
            if (allowedIps != null && !allowedIps.isEmpty()) {
                String clientIp = getClientIp(request);
                
                if (!allowedIps.contains(clientIp)) {
                    log.warn("IP address {} not allowed for partner: {}", clientIp, partner.getName());
                    throw new Exceptions.IpAddressNotAllowedException(
                        "Your IP address is not authorized to access this resource");
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
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
