package com.distributedinventory.cqrs.core.producers;

import com.distributedinventory.cqrs.core.events.BaseEvent;

public interface EventProducer {
    void produce(String topic, BaseEvent event);
}
