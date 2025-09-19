package com.bankcqrsexample.account.cmd.cmd.api.controllers;

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
                                "used", "256MB",
                                "max", "512MB",
                                "free", "256MB"
                        ),
                        "gc", Map.of(
                                "collections", 42,
                                "time", "125ms"
                        )
                ),
                "kafka", Map.of(
                        "producer", Map.of(
                                "messages_sent", 150,
                                "errors", 0,
                                "avg_latency", "2.5ms"
                        ),
                        "consumer", Map.of(
                                "messages_consumed", 120,
                                "lag", "0ms"
                        )
                ),
                "http", Map.of(
                        "requests_total", 1250,
                        "requests_per_second", 15.5,
                        "avg_response_time", "45ms"
                ),
                "events", Map.of(
                        "events_processed", 89,
                        "events_failed", 1,
                        "events_pending", 0
                ),
                "timestamp", java.time.Instant.now().toString()
        );
        
        return ResponseEntity.ok(metrics);
    }
}
