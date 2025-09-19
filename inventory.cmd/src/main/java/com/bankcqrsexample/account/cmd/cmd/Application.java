package com.bankcqrsexample.account.cmd.cmd;

import com.bankcqrsexample.account.cmd.cmd.api.commands.*;
import com.bankcqrsexample.account.cmd.cmd.infrastructure.ProductCommandDispatcher;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.distributedinventory.cqrs.core.events")
@ComponentScan({"com.distributedinventory.cqrs.core", "com.bankcqrsexample.account.cmd.cmd"})
@EnableJpaRepositories
public class Application {

    @Autowired
    @Lazy
    private ProductCommandDispatcher commandDispatcher;
    
    @Autowired
    @Lazy
    private CommandHandler commandHandler;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @PostConstruct
    public void registerHandlers() {
        commandDispatcher.registerHandler(CreateProductCommand.class, commandHandler::handle);
        commandDispatcher.registerHandler(UpdateStockCommand.class, commandHandler::handle);
    }
}
