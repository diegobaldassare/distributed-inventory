package com.distributedinventory.query.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "product_view")
public class ProductView {
    @Id
    private String id;
    private String name;
    private String description;
    private String category;
    private BigDecimal price;
    private String storeId;
    private Integer amount;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}
