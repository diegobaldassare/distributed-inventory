package com.bankcqrsexample.account.query.query.api.controllers;

import com.bankcqrsexample.account.query.query.api.dto.ProductResponse;
import com.bankcqrsexample.account.query.query.api.dto.ProductListResponse;
import com.bankcqrsexample.account.query.query.domain.ProductView;
import com.bankcqrsexample.account.query.query.domain.ProductViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/v1")
@RequiredArgsConstructor
public class ProductQueryController {

    private final ProductViewRepository productViewRepository;

    @GetMapping("/products")
    public ResponseEntity<ProductListResponse> getAllProducts() {
        List<ProductView> products = productViewRepository.findAll();
        
        if (products.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        List<ProductResponse> productResponses = products.stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ProductListResponse.builder()
                .message("Successfully returned " + products.size() + " product(s)!")
                .products(productResponses)
                .build());
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable String id) {
        return productViewRepository.findById(id)
                .map(product -> ResponseEntity.ok(mapToProductResponse(product)))
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/availability")
    public ResponseEntity<Map<String, Object>> getAvailability(
            @RequestParam String sku,
            @RequestParam(required = false) String storeId) {
        
        // For now, return mock availability data
        // In Phase 3, this will query the proper availability projections
        var availability = Map.of(
                "sku", sku,
                "totalAvailable", 100,
                "perStore", java.util.List.of(
                        Map.of(
                                "storeId", storeId != null ? storeId : "store1",
                                "onHand", 120,
                                "reserved", 20,
                                "available", 100
                        )
                )
        );
        
        return ResponseEntity.ok()
                .header("X-Projection-Lag-ms", "0")
                .header("ETag", "v1")
                .body(availability);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "inventory-query",
                "timestamp", java.time.Instant.now().toString()
        ));
    }

    @PostMapping("/products/test")
    public ResponseEntity<String> createTestProduct() {
        try {
            var testProduct = ProductView.builder()
                    .id("test-product-1")
                    .name("Test Product")
                    .description("A test product")
                    .category("Electronics")
                    .price(new java.math.BigDecimal("29.99"))
                    .storeId("store1")
                    .amount(10)
                    .createdDate(java.time.LocalDateTime.now())
                    .updatedDate(java.time.LocalDateTime.now())
                    .build();
            productViewRepository.save(testProduct);
            return ResponseEntity.ok("Test product created successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error creating test product: " + e.getMessage());
        }
    }

    @PostMapping("/products/sync")
    public ResponseEntity<String> syncProduct(@RequestBody java.util.Map<String, Object> productData) {
        try {
            var productView = ProductView.builder()
                    .id((String) productData.get("id"))
                    .name((String) productData.get("name"))
                    .description((String) productData.get("description"))
                    .category((String) productData.get("category"))
                    .price(productData.get("price") != null ? new java.math.BigDecimal(productData.get("price").toString()) : null)
                    .storeId((String) productData.get("storeId"))
                    .amount(productData.get("amount") != null ? (Integer) productData.get("amount") : 0)
                    .createdDate(productData.get("createdDate") != null ? 
                        java.time.LocalDateTime.now() : 
                        java.time.LocalDateTime.now())
                    .updatedDate(java.time.LocalDateTime.now())
                    .build();
            productViewRepository.save(productView);
            System.out.println("Product synchronized successfully: " + productData.get("id"));
            return ResponseEntity.ok("Product synchronized successfully!");
        } catch (Exception e) {
            System.out.println("Error synchronizing product: " + e.getMessage());
            return ResponseEntity.status(500).body("Error synchronizing product: " + e.getMessage());
        }
    }

    private ProductResponse mapToProductResponse(ProductView product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .category(product.getCategory())
                .price(product.getPrice())
                .storeId(product.getStoreId())
                .amount(product.getAmount())
                .createdDate(product.getCreatedDate())
                .updatedDate(product.getUpdatedDate())
                .build();
    }
}
