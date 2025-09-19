package com.bankcqrsexample.account.query.query.api.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class HealthController {
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "inventory-query",
                "timestamp", java.time.Instant.now().toString()
        ));
    }
    
    @GetMapping("/ready")
    public ResponseEntity<Map<String, String>> ready() {
        // In a real implementation, check dependencies (database, Kafka, etc.)
        return ResponseEntity.ok(Map.of(
                "status", "READY",
                "service", "inventory-query",
                "dependencies", "UP"
        ));
    }
}
