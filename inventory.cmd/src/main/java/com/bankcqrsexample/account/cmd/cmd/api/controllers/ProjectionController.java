package com.bankcqrsexample.account.cmd.cmd.api.controllers;

import com.distributedinventory.cqrs.core.handlers.EventSourcingHandler;
import com.bankcqrsexample.account.cmd.cmd.domain.ProductAggregate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/projections")
@RequiredArgsConstructor
public class ProjectionController {
    
    private final EventSourcingHandler<ProductAggregate> eventSourcingHandler;
    
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getProjectionStatus() {
        // In a real implementation, you'd check the actual projection status
        var status = Map.<String, Object>of(
                "status", "UP",
                "lastProcessedEvent", "event-123",
                "lag", "0ms",
                "lastUpdated", java.time.Instant.now().toString()
        );
        
        return ResponseEntity.ok(status);
    }
    
    @PostMapping("/rebuild")
    public ResponseEntity<Map<String, String>> rebuildProjections() {
        try {
            // Trigger projection rebuild by republishing all events
            eventSourcingHandler.republishEvents();
            
            return ResponseEntity.accepted().body(Map.of(
                    "message", "Projection rebuild initiated",
                    "timestamp", java.time.Instant.now().toString()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to rebuild projections: " + e.getMessage()
            ));
        }
    }
}
