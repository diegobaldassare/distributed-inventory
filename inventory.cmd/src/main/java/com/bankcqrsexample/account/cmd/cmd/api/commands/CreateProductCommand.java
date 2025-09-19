package com.bankcqrsexample.account.cmd.cmd.api.commands;

import com.distributedinventory.cqrs.core.commands.BaseCommand;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateProductCommand extends BaseCommand {
    
    public CreateProductCommand(String id, String name, String description, String category, 
                               BigDecimal price, String storeId, Integer initialAmount) {
        super(id);
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.storeId = storeId;
        this.initialAmount = initialAmount;
    }
    
    private String name;
    private String description;
    private String category;
    private BigDecimal price;
    private String storeId;
    private Integer initialAmount;
}
