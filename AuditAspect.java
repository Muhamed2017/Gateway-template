package com.enterprise.gateway.aspect;

import com.enterprise.gateway.model.Partner;
import com.enterprise.gateway.service.AuditService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Aspect for auditing all controller requests
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    @Around("execution(* com.enterprise.gateway.controller..*(..))")
    public Object auditRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return joinPoint.proceed();
        }

        String method = request.getMethod();
        String endpoint = request.getRequestURI();
        String queryParams = request.getQueryString();
        String clientIp = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");

        Partner partner = getCurrentPartner();
        Object response = null;
        Integer httpStatus = 200;
        String errorMessage = null;

        try {
            response = joinPoint.proceed();
            
            HttpServletResponse httpResponse = getCurrentResponse();
            if (httpResponse != null) {
                httpStatus = httpResponse.getStatus();
            }
            
            return response;
        } catch (Exception e) {
            httpStatus = 500;
            errorMessage = e.getMessage();
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            
            String requestBody = extractRequestBody(joinPoint.getArgs());
            String responseBody = extractResponseBody(response);

            auditService.logRequest(
                partner,
                method,
                endpoint,
                queryParams,
                requestBody,
                responseBody,
                httpStatus,
                clientIp,
                userAgent,
                duration,
                errorMessage,
                null,
                false
            );
        }
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = 
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    private HttpServletResponse getCurrentResponse() {
        ServletRequestAttributes attributes = 
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getResponse() : null;
    }

    private Partner getCurrentPartner() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Partner) {
            return (Partner) authentication.getPrincipal();
        }
        return null;
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String extractRequestBody(Object[] args) {
        try {
            for (Object arg : args) {
                if (arg != null && 
                    !arg.getClass().getName().startsWith("org.springframework") &&
                    !arg.getClass().getName().startsWith("jakarta.servlet")) {
                    return objectMapper.writeValueAsString(arg);
                }
            }
        } catch (Exception e) {
            log.debug("Could not serialize request body: {}", e.getMessage());
        }
        return null;
    }

    private String extractResponseBody(Object response) {
        try {
            if (response != null) {
                return objectMapper.writeValueAsString(response);
            }
        } catch (Exception e) {
            log.debug("Could not serialize response body: {}", e.getMessage());
        }
        return null;
    }
}
