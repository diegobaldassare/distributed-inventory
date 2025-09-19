package com.bankcqrsexample.account.query.query.infrastructure;

import com.distributedinventory.cqrs.core.events.BaseEvent;
import com.distributedinventory.cqrs.core.events.EventModel;
import com.distributedinventory.cqrs.core.infrastructure.EventStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SimpleEventStore implements EventStore {
    
    // In-memory storage for demo purposes
    private final Map<String, List<EventModel>> eventStore = new ConcurrentHashMap<>();
    private final Map<String, Integer> versionStore = new ConcurrentHashMap<>();
    
    @Override
    public void saveEvents(String aggregateId, Iterable<BaseEvent> events, int expectedVersion) {
        // This is a read-only service, so we don't implement saving
        throw new UnsupportedOperationException("Query service is read-only");
    }
    
    @Override
    public List<BaseEvent> getEvents(String aggregateId) {
        // Return empty list for demo purposes
        return new ArrayList<>();
    }
    
    @Override
    public List<String> getAggregateIds() {
        return new ArrayList<>(eventStore.keySet());
    }
    
    @Override
    public List<EventModel> findAll() {
        return new ArrayList<>();
    }
    
    @Override
    public List<EventModel> findByAggregateIdentifier(String aggregateId) {
        return new ArrayList<>();
    }
    
    @Override
    public int getCurrentVersion(String aggregateId) {
        return versionStore.getOrDefault(aggregateId, -1);
    }
}
