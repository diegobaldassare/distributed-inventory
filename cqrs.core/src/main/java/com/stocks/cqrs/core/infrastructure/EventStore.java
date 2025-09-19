package com.distributedinventory.cqrs.core.infrastructure;

import com.distributedinventory.cqrs.core.events.BaseEvent;

import java.util.List;

public interface EventStore {
    void saveEvents(String aggregateId, Iterable<BaseEvent> events, int expectedVersion);
    List<BaseEvent> getEvents(String aggregateId);
    List<String> getAggregateIds();
    // This method should be implemented by concrete EventStore implementations
    // It's not in the interface because it returns a specific aggregate type
}
