package com.distributedinventory.query.handlers;

import com.distributedinventory.cqrs.core.events.ProductCreatedEvent;
import com.distributedinventory.cqrs.core.events.StockUpdatedEvent;
import com.distributedinventory.query.domain.ProductView;
import com.distributedinventory.query.domain.ProductViewRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class ProductEventHandler {

    private final ProductViewRepository productViewRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "ProductCreatedEvent", groupId = "inventoryConsumer")
    public void handleProductCreated(ProductCreatedEvent event) {
        System.out.println("=== RECEIVED PRODUCT CREATED EVENT: " + event + " ===");
        try {
            System.out.println("ProductEventHandler.handle() called for ProductCreatedEvent: " + event.getId());
            var productView = ProductView.builder()
                    .id(event.getId())
                    .name(event.getName())
                    .description(event.getDescription())
                    .category(event.getCategory())
                    .price(event.getPrice())
                    .storeId(event.getStoreId())
                    .amount(event.getInitialAmount())
                    .createdDate(event.getCreatedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                    .updatedDate(event.getCreatedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                    .build();
            productViewRepository.save(productView);
            System.out.println("ProductView saved for product: " + event.getId());
        } catch (Exception e) {
            System.out.println("Error processing ProductCreatedEvent: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Generic listener to test if any messages are received
    @KafkaListener(topics = {"ProductCreatedEvent", "StockUpdatedEvent"}, groupId = "inventoryConsumer")
    public void handleAnyMessage(String message) {
        System.out.println("=== GENERIC LISTENER RECEIVED: " + message + " ===");
    }

    // Test listener with different group ID
    @KafkaListener(topics = "ProductCreatedEvent", groupId = "testConsumer")
    public void handleTestMessage(String message) {
        System.out.println("=== TEST CONSUMER RECEIVED: " + message + " ===");
    }

    @KafkaListener(topics = "StockUpdatedEvent", groupId = "inventoryConsumer")
    public void handleStockUpdated(StockUpdatedEvent event) {
        System.out.println("=== RECEIVED STOCK UPDATE EVENT: " + event + " ===");
        try {
            System.out.println("ProductEventHandler.handle() called for StockUpdatedEvent: " + event.getId());
            var productView = productViewRepository.findById(event.getId()).orElse(null);
            if (productView != null) {
                productView.setAmount(event.getNewAmount());
                productView.setUpdatedDate(event.getUpdatedDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
                productViewRepository.save(productView);
                System.out.println("Product synchronized successfully: " + event.getId());
            }
        } catch (Exception e) {
            System.out.println("Error processing StockUpdatedEvent: " + e.getMessage());
            e.printStackTrace();
        }
    }


}
