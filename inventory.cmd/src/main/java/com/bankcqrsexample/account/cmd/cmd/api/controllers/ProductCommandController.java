package com.bankcqrsexample.account.cmd.cmd.api.controllers;

import com.bankcqrsexample.account.cmd.cmd.api.commands.CreateProductCommand;
import com.bankcqrsexample.account.cmd.cmd.api.commands.UpdateStockCommand;
import com.bankcqrsexample.account.cmd.cmd.domain.ProductAggregate;
import com.bankcqrsexample.account.cmd.cmd.infrastructure.ProductCommandDispatcher;
import com.distributedinventory.cqrs.core.exceptions.ConcurrencyException;
import com.distributedinventory.cqrs.core.infrastructure.IdempotencyService;
import com.distributedinventory.cqrs.core.handlers.EventSourcingHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(path = "/v1")
@RequiredArgsConstructor
public class ProductCommandController {

    private final ProductCommandDispatcher commandDispatcher;
    private final IdempotencyService idempotencyService;
    private final EventSourcingHandler<ProductAggregate> eventSourcingHandler;

    @PostMapping("/products")
    public ResponseEntity<Map<String, String>> createProduct(
            @RequestBody CreateProductRequest request,
            @RequestHeader(value = "If-Match", required = false) String ifMatch,
            HttpServletRequest httpRequest) {
        
        String idempotencyKey = (String) httpRequest.getAttribute("idempotencyKey");
        
        try {
            var command = new CreateProductCommand(
                    UUID.randomUUID().toString(),
                    request.getName(),
                    request.getDescription(),
                    request.getCategory(),
                    request.getPrice(),
                    request.getStoreId(),
                    request.getInitialAmount()
            );
            
            commandDispatcher.send(command);
            
            Map<String, String> response = Map.of(
                    "message", "Product creation command accepted",
                    "productId", command.getId(),
                    "version", "1"
            );
            
            // Store successful result for idempotency
            if (idempotencyKey != null) {
                idempotencyService.storeResult(idempotencyKey, 
                    IdempotencyService.IdempotencyResult.success(202, response));
            }
            
            return ResponseEntity.accepted()
                    .header("ETag", "1")
                    .body(response);
                    
        } catch (ConcurrencyException e) {
            // Handle optimistic locking conflicts
            Map<String, String> errorResponse = Map.of(
                    "error", "Version conflict",
                    "message", e.getMessage()
            );
            
            if (idempotencyKey != null) {
                idempotencyService.storeResult(idempotencyKey, 
                    IdempotencyService.IdempotencyResult.error(409, "Version conflict"));
            }
            
            return ResponseEntity.status(409)
                    .header("ETag", "current-version") // In real implementation, get actual current version
                    .body(errorResponse);
                    
        } catch (Exception e) {
            Map<String, String> errorResponse = Map.of(
                    "error", "Failed to create product: " + e.getMessage()
            );
            
            if (idempotencyKey != null) {
                idempotencyService.storeResult(idempotencyKey, 
                    IdempotencyService.IdempotencyResult.error(400, e.getMessage()));
            }
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PutMapping("/products/{id}/stock")
    public ResponseEntity<Map<String, String>> updateStock(
            @PathVariable String id,
            @RequestBody UpdateStockRequest request,
            @RequestHeader(value = "If-Match", required = false) String ifMatch,
            HttpServletRequest httpRequest) {
        
        String idempotencyKey = (String) httpRequest.getAttribute("idempotencyKey");
        
        try {
            // Check optimistic locking if If-Match header is provided
            if (ifMatch != null) {
                ProductAggregate aggregate = eventSourcingHandler.getById(id);
                if (aggregate != null) {
                    int expectedVersion = Integer.parseInt(ifMatch);
                    aggregate.validateVersion(expectedVersion);
                }
            }
            
            var command = new UpdateStockCommand(
                    id,
                    request.getOperation(),
                    request.getAmount(),
                    request.getReason()
            );
            
            commandDispatcher.send(command);
            
            Map<String, String> response = Map.of(
                    "message", "Stock update command accepted",
                    "productId", id,
                    "version", "2" // In real implementation, get actual new version
            );
            
            // Store successful result for idempotency
            if (idempotencyKey != null) {
                idempotencyService.storeResult(idempotencyKey, 
                    IdempotencyService.IdempotencyResult.success(202, response));
            }
            
            return ResponseEntity.accepted()
                    .header("ETag", "2")
                    .body(response);
                    
        } catch (ConcurrencyException e) {
            // Handle optimistic locking conflicts
            Map<String, String> errorResponse = Map.of(
                    "error", "Version conflict",
                    "message", e.getMessage()
            );
            
            if (idempotencyKey != null) {
                idempotencyService.storeResult(idempotencyKey, 
                    IdempotencyService.IdempotencyResult.error(409, "Version conflict"));
            }
            
            return ResponseEntity.status(409)
                    .header("ETag", "current-version") // In real implementation, get actual current version
                    .body(errorResponse);
                    
        } catch (Exception e) {
            Map<String, String> errorResponse = Map.of(
                    "error", "Failed to update stock: " + e.getMessage()
            );
            
            if (idempotencyKey != null) {
                idempotencyService.storeResult(idempotencyKey, 
                    IdempotencyService.IdempotencyResult.error(400, e.getMessage()));
            }
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "inventory-cmd",
                "timestamp", java.time.Instant.now().toString()
        ));
    }

    // Request DTOs
    public static class CreateProductRequest {
        private String name;
        private String description;
        private String category;
        private java.math.BigDecimal price;
        private String storeId;
        private Integer initialAmount;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public java.math.BigDecimal getPrice() { return price; }
        public void setPrice(java.math.BigDecimal price) { this.price = price; }
        public String getStoreId() { return storeId; }
        public void setStoreId(String storeId) { this.storeId = storeId; }
        public Integer getInitialAmount() { return initialAmount; }
        public void setInitialAmount(Integer initialAmount) { this.initialAmount = initialAmount; }
    }

    public static class UpdateStockRequest {
        private String operation;
        private Integer amount;
        private String reason;

        // Getters and setters
        public String getOperation() { return operation; }
        public void setOperation(String operation) { this.operation = operation; }
        public Integer getAmount() { return amount; }
        public void setAmount(Integer amount) { this.amount = amount; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}
