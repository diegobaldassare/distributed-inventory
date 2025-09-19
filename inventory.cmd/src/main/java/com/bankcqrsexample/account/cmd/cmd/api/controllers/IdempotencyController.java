package com.bankcqrsexample.account.cmd.cmd.api.controllers;

import com.distributedinventory.cqrs.core.infrastructure.IdempotencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/idempotency")
@RequiredArgsConstructor
public class IdempotencyController {
    
    private final IdempotencyService idempotencyService;
    
    @GetMapping("/{key}")
    public ResponseEntity<Map<String, Object>> getIdempotencyResult(@PathVariable String key) {
        IdempotencyService.IdempotencyResult result = idempotencyService.getResult(key);
        
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        
        var response = Map.of(
                "key", key,
                "status", result.getStatus().toString(),
                "httpStatusCode", result.getHttpStatusCode(),
                "responseBody", result.getResponseBody() != null ? result.getResponseBody() : "null",
                "errorMessage", result.getErrorMessage() != null ? result.getErrorMessage() : "null"
        );
        
        return ResponseEntity.ok(response);
    }
}
