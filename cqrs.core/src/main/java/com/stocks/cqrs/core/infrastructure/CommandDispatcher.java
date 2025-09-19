package com.distributedinventory.cqrs.core.infrastructure;

import com.distributedinventory.cqrs.core.commands.BaseCommand;
import com.distributedinventory.cqrs.core.commands.CommandHandlerMethod;

// Mediator
public interface CommandDispatcher {
    <T extends BaseCommand> void registerHandler(Class<T> type, CommandHandlerMethod<T> handler);
    void send(BaseCommand command);
}
