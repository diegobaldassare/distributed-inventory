package com.bankcqrsexample.account.query.query.api.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
public class MetricsController {
    
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getMetrics() {
        // In a real implementation, you'd collect actual metrics from Micrometer, Prometheus, etc.
        var metrics = Map.<String, Object>of(
                "jvm", Map.of(
                        "memory", Map.of(
                                "used", "128MB",
                                "max", "256MB",
                                "free", "128MB"
                        ),
                        "gc", Map.of(
                                "collections", 25,
                                "time", "75ms"
                        )
                ),
                "database", Map.of(
                        "connections", Map.of(
                                "active", 5,
                                "idle", 10,
                                "max", 20
                        ),
                        "queries", Map.of(
                                "total", 450,
                                "avg_time", "12ms"
                        )
                ),
                "http", Map.of(
                        "requests_total", 890,
                        "requests_per_second", 12.3,
                        "avg_response_time", "25ms"
                ),
                "projections", Map.of(
                        "events_processed", 67,
                        "lag", "0ms",
                        "last_updated", java.time.Instant.now().toString()
                ),
                "timestamp", java.time.Instant.now().toString()
        );
        
        return ResponseEntity.ok(metrics);
    }
}
