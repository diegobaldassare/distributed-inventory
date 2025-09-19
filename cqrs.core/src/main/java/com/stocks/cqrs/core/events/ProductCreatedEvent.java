package com.distributedinventory.cqrs.core.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ProductCreatedEvent extends BaseEvent {
    private String name;
    private String description;
    private String category;
    private BigDecimal price;
    private String storeId;
    private Integer initialAmount;
    private Date createdDate;
}