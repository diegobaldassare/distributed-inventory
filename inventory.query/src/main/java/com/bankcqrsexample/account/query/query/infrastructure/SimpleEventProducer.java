package com.bankcqrsexample.account.query.query.infrastructure;

import com.distributedinventory.cqrs.core.events.BaseEvent;
import com.distributedinventory.cqrs.core.events.EventModel;
import com.distributedinventory.cqrs.core.producers.EventProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SimpleEventProducer implements EventProducer {
    
    @Override
    public void produce(String topic, BaseEvent event) {
        // Query service doesn't produce events, just log for demo
        log.info("Query service received event: {} for topic: {}", event.getClass().getSimpleName(), topic);
    }
    
    @Override
    public void produce(String topic, EventModel eventModel) {
        // Query service doesn't produce events, just log for demo
        log.info("Query service received event model: {} for topic: {}", eventModel.getEventType(), topic);
    }
}
