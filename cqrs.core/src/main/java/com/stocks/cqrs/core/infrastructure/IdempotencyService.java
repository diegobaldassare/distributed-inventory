package com.distributedinventory.cqrs.core.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class IdempotencyService {
    
    // In-memory storage for demo purposes
    // In production, this should be stored in a persistent database
    private final Map<String, IdempotencyResult> idempotencyStore = new ConcurrentHashMap<>();
    
    /**
     * Check if an idempotency key has already been processed
     * @param key The idempotency key
     * @return The stored result if exists, null otherwise
     */
    public IdempotencyResult getResult(String key) {
        return idempotencyStore.get(key);
    }
    
    /**
     * Store the result of an idempotent operation
     * @param key The idempotency key
     * @param result The operation result
     */
    public void storeResult(String key, IdempotencyResult result) {
        idempotencyStore.put(key, result);
    }
    
    /**
     * Check if an operation is currently being processed (to prevent duplicate processing)
     * @param key The idempotency key
     * @return true if currently processing, false otherwise
     */
    public boolean isProcessing(String key) {
        IdempotencyResult result = idempotencyStore.get(key);
        return result != null && result.getStatus() == IdempotencyStatus.PROCESSING;
    }
    
    /**
     * Mark an operation as currently being processed
     * @param key The idempotency key
     */
    public void markAsProcessing(String key) {
        idempotencyStore.put(key, IdempotencyResult.processing());
    }
    
    public static class IdempotencyResult {
        private final IdempotencyStatus status;
        private final int httpStatusCode;
        private final Object responseBody;
        private final String errorMessage;
        
        private IdempotencyResult(IdempotencyStatus status, int httpStatusCode, Object responseBody, String errorMessage) {
            this.status = status;
            this.httpStatusCode = httpStatusCode;
            this.responseBody = responseBody;
            this.errorMessage = errorMessage;
        }
        
        public static IdempotencyResult success(int httpStatusCode, Object responseBody) {
            return new IdempotencyResult(IdempotencyStatus.SUCCESS, httpStatusCode, responseBody, null);
        }
        
        public static IdempotencyResult error(int httpStatusCode, String errorMessage) {
            return new IdempotencyResult(IdempotencyStatus.ERROR, httpStatusCode, null, errorMessage);
        }
        
        public static IdempotencyResult processing() {
            return new IdempotencyResult(IdempotencyStatus.PROCESSING, 0, null, null);
        }
        
        // Getters
        public IdempotencyStatus getStatus() { return status; }
        public int getHttpStatusCode() { return httpStatusCode; }
        public Object getResponseBody() { return responseBody; }
        public String getErrorMessage() { return errorMessage; }
    }
    
    public enum IdempotencyStatus {
        PROCESSING,
        SUCCESS,
        ERROR
    }
}
