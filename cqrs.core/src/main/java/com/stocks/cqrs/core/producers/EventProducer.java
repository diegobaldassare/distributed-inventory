package com.distributedinventory.cqrs.core.producers;

import com.distributedinventory.cqrs.core.events.BaseEvent;
import com.distributedinventory.cqrs.core.events.EventModel;

public interface EventProducer {
    void produce(String topic, BaseEvent event);
    void produce(String topic, EventModel eventModel);
}
