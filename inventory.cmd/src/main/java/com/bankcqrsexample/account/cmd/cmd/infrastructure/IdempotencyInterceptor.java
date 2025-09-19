package com.bankcqrsexample.account.cmd.cmd.infrastructure;

import com.distributedinventory.cqrs.core.infrastructure.IdempotencyService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class IdempotencyInterceptor implements HandlerInterceptor {
    
    private final IdempotencyService idempotencyService;
    private final ObjectMapper objectMapper;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        
        // Only apply to POST, PUT, PATCH requests
        if (!isWriteRequest(request.getMethod())) {
            return true;
        }
        
        String idempotencyKey = request.getHeader("Idempotency-Key");
        
        // Idempotency key is required for write operations
        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"error\":\"Idempotency-Key header is required for write operations\"}");
            return false;
        }
        
        // Check if this operation has already been processed
        IdempotencyService.IdempotencyResult existingResult = idempotencyService.getResult(idempotencyKey);
        
        if (existingResult != null) {
            switch (existingResult.getStatus()) {
                case SUCCESS:
                    // Return the cached successful result
                    response.setStatus(existingResult.getHttpStatusCode());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    String successBody = objectMapper.writeValueAsString(existingResult.getResponseBody());
                    response.getWriter().write(successBody);
                    log.info("Returning cached successful result for idempotency key: {}", idempotencyKey);
                    return false;
                    
                case ERROR:
                    // Return the cached error result
                    response.setStatus(existingResult.getHttpStatusCode());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    String errorBody = "{\"error\":\"" + existingResult.getErrorMessage() + "\"}";
                    response.getWriter().write(errorBody);
                    log.info("Returning cached error result for idempotency key: {}", idempotencyKey);
                    return false;
                    
                case PROCESSING:
                    // Another request is currently processing - return 409 Conflict
                    response.setStatus(HttpStatus.CONFLICT.value());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write("{\"error\":\"Request is currently being processed\"}");
                    log.warn("Request already being processed for idempotency key: {}", idempotencyKey);
                    return false;
            }
        }
        
        // Mark this request as being processed
        idempotencyService.markAsProcessing(idempotencyKey);
        request.setAttribute("idempotencyKey", idempotencyKey);
        
        log.info("Processing new request with idempotency key: {}", idempotencyKey);
        return true;
    }
    
    private boolean isWriteRequest(String method) {
        return "POST".equalsIgnoreCase(method) || 
               "PUT".equalsIgnoreCase(method) || 
               "PATCH".equalsIgnoreCase(method);
    }
}
