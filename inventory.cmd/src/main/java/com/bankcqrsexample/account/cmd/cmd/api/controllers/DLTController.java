package com.bankcqrsexample.account.cmd.cmd.api.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/v1/dlt")
@RequiredArgsConstructor
public class DLTController {
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getDLTStatus() {
        // In a real implementation, you'd check actual DLT status
        var status = Map.<String, Object>of(
                "status", "UP",
                "totalMessages", 0,
                "failedMessages", 0,
                "lastProcessed", java.time.Instant.now().toString(),
                "topics", List.of("stock-events-dlt", "reservation-events-dlt", "transfer-events-dlt")
        );
        
        return ResponseEntity.ok(status);
    }
    
    @PostMapping("/replay")
    public ResponseEntity<Map<String, String>> replayDLTMessages(
            @RequestParam(required = false) String topic,
            @RequestParam(required = false, defaultValue = "0") int offset) {
        
        try {
            log.info("Replaying DLT messages for topic: {}, offset: {}", topic, offset);
            
            // In a real implementation, you'd replay messages from DLT
            var response = Map.of(
                    "message", "DLT replay initiated",
                    "topic", topic != null ? topic : "all-topics",
                    "offset", String.valueOf(offset),
                    "timestamp", java.time.Instant.now().toString()
            );
            
            return ResponseEntity.accepted().body(response);
            
        } catch (Exception e) {
            log.error("Error replaying DLT messages: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Failed to replay DLT messages: " + e.getMessage()
            ));
        }
    }
}
