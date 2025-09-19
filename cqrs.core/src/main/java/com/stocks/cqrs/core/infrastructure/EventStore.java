package com.distributedinventory.cqrs.core.infrastructure;

import com.distributedinventory.cqrs.core.events.BaseEvent;
import com.distributedinventory.cqrs.core.events.EventModel;

import java.util.List;

public interface EventStore {
    void saveEvents(String aggregateId, Iterable<BaseEvent> events, int expectedVersion);
    List<BaseEvent> getEvents(String aggregateId);
    List<String> getAggregateIds();
    List<EventModel> findAll();
    List<EventModel> findByAggregateIdentifier(String aggregateId);
    int getCurrentVersion(String aggregateId);
}
