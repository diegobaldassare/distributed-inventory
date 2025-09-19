package com.bankcqrsexample.account.cmd.cmd.api.controllers;

import com.distributedinventory.cqrs.core.infrastructure.EventStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/v1/events")
@RequiredArgsConstructor
public class EventsController {
    
    private final EventStore eventStore;
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getEvents(
            @RequestParam(required = false) String aggregateId,
            @RequestParam(required = false, defaultValue = "0") int offset,
            @RequestParam(required = false, defaultValue = "100") int limit) {
        
        try {
            List<com.distributedinventory.cqrs.core.events.EventModel> events;
            
            if (aggregateId != null) {
                events = eventStore.findByAggregateIdentifier(aggregateId);
            } else {
                events = eventStore.findAll();
            }
            
            // Apply pagination
            int start = Math.min(offset, events.size());
            int end = Math.min(start + limit, events.size());
            List<com.distributedinventory.cqrs.core.events.EventModel> paginatedEvents = 
                events.subList(start, end);
            
            var response = Map.<String, Object>of(
                    "events", paginatedEvents,
                    "total", events.size(),
                    "offset", offset,
                    "limit", limit,
                    "hasMore", end < events.size(),
                    "timestamp", java.time.Instant.now().toString()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error retrieving events: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to retrieve events: " + e.getMessage()
            ));
        }
    }
    
    @GetMapping("/{eventId}")
    public ResponseEntity<Map<String, Object>> getEventById(@PathVariable String eventId) {
        try {
            // In a real implementation, you'd retrieve the specific event by ID
            var event = Map.<String, Object>of(
                    "id", eventId,
                    "type", "ProductCreatedEvent",
                    "aggregateId", "sample-aggregate-id",
                    "version", 1,
                    "timestamp", java.time.Instant.now().toString(),
                    "data", Map.of(
                            "name", "Sample Product",
                            "price", 99.99
                    )
            );
            
            return ResponseEntity.ok(event);
            
        } catch (Exception e) {
            log.error("Error retrieving event {}: {}", eventId, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
