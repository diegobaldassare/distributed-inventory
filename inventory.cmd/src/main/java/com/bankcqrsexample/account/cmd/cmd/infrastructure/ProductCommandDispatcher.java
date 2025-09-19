package com.bankcqrsexample.account.cmd.cmd.infrastructure;

import com.distributedinventory.cqrs.core.commands.BaseCommand;
import com.distributedinventory.cqrs.core.commands.CommandHandlerMethod;
import com.bankcqrsexample.account.cmd.cmd.api.commands.CommandHandler;
import com.bankcqrsexample.account.cmd.cmd.api.commands.CreateProductCommand;
import com.bankcqrsexample.account.cmd.cmd.api.commands.UpdateStockCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductCommandDispatcher implements com.distributedinventory.cqrs.core.infrastructure.CommandDispatcher {
    
    private final CommandHandler commandHandler;
    private final Map<Class<? extends BaseCommand>, CommandHandlerMethod<BaseCommand>> routes = new HashMap<>();

    @Override
    public <T extends BaseCommand> void registerHandler(Class<T> type, CommandHandlerMethod<T> handler) {
        routes.put((Class<? extends BaseCommand>) type, (CommandHandlerMethod<BaseCommand>) handler);
    }


    @Override
    public void send(BaseCommand command) {
        var handler = routes.get(command.getClass());
        if (handler == null) {
            throw new RuntimeException("No command handler was registered!");
        }
        handler.handle(command);
    }

    @PostConstruct
    public void registerHandlers() {
        routes.put(CreateProductCommand.class, (CommandHandlerMethod<BaseCommand>) command -> commandHandler.handle((CreateProductCommand) command));
        routes.put(UpdateStockCommand.class, (CommandHandlerMethod<BaseCommand>) command -> commandHandler.handle((UpdateStockCommand) command));
    }
}