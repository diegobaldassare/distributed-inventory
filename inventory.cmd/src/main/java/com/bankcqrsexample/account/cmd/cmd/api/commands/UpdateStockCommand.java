package com.bankcqrsexample.account.cmd.cmd.api.commands;

import com.distributedinventory.cqrs.core.commands.BaseCommand;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateStockCommand extends BaseCommand {
    
    public UpdateStockCommand(String id, String operation, Integer amount, String reason) {
        super(id);
        this.operation = operation;
        this.amount = amount;
        this.reason = reason;
    }
    
    private String operation;
    private Integer amount;
    private String reason;
}
