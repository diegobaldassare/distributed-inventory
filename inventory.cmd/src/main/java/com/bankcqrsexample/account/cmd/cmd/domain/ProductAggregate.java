package com.bankcqrsexample.account.cmd.cmd.domain;

import com.distributedinventory.cqrs.core.domain.VersionedAggregateRoot;
import com.distributedinventory.cqrs.core.events.ProductCreatedEvent;
import com.distributedinventory.cqrs.core.events.StockUpdatedEvent;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
public class ProductAggregate extends VersionedAggregateRoot {
    
    private String name;
    private String description;
    private String category;
    private BigDecimal price;
    private String storeId;
    private Integer amount;

    public ProductAggregate(String id, String name, String description, String category, 
                          BigDecimal price, String storeId, Integer initialAmount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.storeId = storeId;
        this.amount = initialAmount;
        
        // Raise the product created event
        var event = ProductCreatedEvent.builder()
                .id(id)
                .name(name)
                .description(description)
                .category(category)
                .price(price)
                .storeId(storeId)
                .initialAmount(initialAmount)
                .build();
        
        raiseEvent(event);
    }

    public void updateStock(String operation, Integer amount, String reason) {
        if (this.amount == null) {
            this.amount = 0;
        }
        
        int newAmount;
        switch (operation.toLowerCase()) {
            case "purchase":
                newAmount = this.amount - amount;
                break;
            case "restock":
                newAmount = this.amount + amount;
                break;
            case "set":
                newAmount = amount;
                break;
            default:
                throw new IllegalArgumentException("Unknown operation: " + operation);
        }
        
        if (newAmount < 0) {
            throw new IllegalArgumentException("Insufficient stock. Current: " + this.amount + ", Requested: " + amount);
        }
        
        this.amount = newAmount;
        
        // Raise the stock updated event
        var event = StockUpdatedEvent.builder()
                .id(this.getId())
                .operation(operation)
                .amount(amount)
                .newAmount(this.amount)
                .reason(reason)
                .build();
        
        raiseEvent(event);
    }
}
