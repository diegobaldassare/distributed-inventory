package com.bankcqrsexample.account.cmd.cmd.api.commands;

import com.bankcqrsexample.account.cmd.cmd.domain.ProductAggregate;
import com.distributedinventory.cqrs.core.handlers.EventSourcingHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
@Lazy
@RequiredArgsConstructor
public class CommandHandler {
    private final EventSourcingHandler<ProductAggregate> eventSourcingHandler;

    public void handle(CreateProductCommand command) {
        var aggregate = new ProductAggregate(command.getId(), command.getName(), command.getDescription(), command.getCategory(), command.getPrice(), command.getStoreId(), command.getInitialAmount());
        eventSourcingHandler.save(aggregate);
    }

    public void handle(UpdateStockCommand command) {
        var aggregate = eventSourcingHandler.getById(command.getId());
        aggregate.updateStock(command.getOperation(), command.getAmount(), command.getReason());
        eventSourcingHandler.save(aggregate);
    }
}
