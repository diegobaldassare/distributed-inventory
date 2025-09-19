package com.distributedinventory.cqrs.core.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class StockUpdatedEvent extends BaseEvent {
    private String operation; // e.g., "purchase", "sale", "devolution", "refill", "adjustment"
    private Integer amount;
    private Integer newAmount;
    private String reason; // Optional reason for the stock update
    private Date updatedDate;
}