package com.bankcqrsexample.account.cmd.cmd.infrastructure;

import com.distributedinventory.cqrs.core.domain.AggregateRoot;
import com.distributedinventory.cqrs.core.events.BaseEvent;
import com.distributedinventory.cqrs.core.events.EventModel;
import com.distributedinventory.cqrs.core.exceptions.ConcurrencyException;
import com.distributedinventory.cqrs.core.infrastructure.EventStore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventStoreImpl implements EventStore {
    
    private final ObjectMapper objectMapper;
    
    // In production, this should be a persistent database
    // For demo purposes, using in-memory storage
    private final Map<String, List<EventModel>> eventStore = new ConcurrentHashMap<>();
    private final Map<String, Integer> versionStore = new ConcurrentHashMap<>();
    
    @Override
    public void saveEvents(String aggregateId, Iterable<BaseEvent> events, int expectedVersion) {
        List<EventModel> eventModels = new ArrayList<>();
        
        // Check version for optimistic locking
        Integer currentVersion = versionStore.getOrDefault(aggregateId, -1);
        if (expectedVersion != -1 && currentVersion != expectedVersion) {
            throw new ConcurrencyException("Version mismatch. Expected: " + expectedVersion + ", Current: " + currentVersion);
        }
        
        // Convert events to EventModels and save
        for (BaseEvent event : events) {
            try {
                EventModel eventModel = EventModel.builder()
                        .id(null) // Let JPA generate the ID
                        .timeStamp(new java.util.Date())
                        .aggregateIdentifier(aggregateId)
                        .aggregateType(event.getClass().getSimpleName())
                        .version(currentVersion + 1)
                        .eventType(event.getClass().getSimpleName())
                        .eventData(objectMapper.writeValueAsString(event))
                        .build();
                
                eventModels.add(eventModel);
                currentVersion++;
                
                log.info("Saving event: {} for aggregate: {} with version: {}", 
                        event.getClass().getSimpleName(), aggregateId, currentVersion);
                        
            } catch (JsonProcessingException e) {
                log.error("Error serializing event: {}", e.getMessage());
                throw new RuntimeException("Error serializing event", e);
            }
        }
        
        // Store events and update version
        eventStore.computeIfAbsent(aggregateId, k -> new ArrayList<>()).addAll(eventModels);
        versionStore.put(aggregateId, currentVersion);
        
        log.info("Saved {} events for aggregate: {} with final version: {}", 
                eventModels.size(), aggregateId, currentVersion);
    }
    
    @Override
    public List<BaseEvent> getEvents(String aggregateId) {
        List<EventModel> eventModels = eventStore.getOrDefault(aggregateId, new ArrayList<>());
        
        return eventModels.stream()
                .map(this::deserializeEvent)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<EventModel> findAll() {
        return eventStore.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<EventModel> findByAggregateIdentifier(String aggregateId) {
        return eventStore.getOrDefault(aggregateId, new ArrayList<>());
    }
    
    @Override
    public int getCurrentVersion(String aggregateId) {
        return versionStore.getOrDefault(aggregateId, -1);
    }
    
    @Override
    public List<String> getAggregateIds() {
        return new ArrayList<>(eventStore.keySet());
    }
    
    private BaseEvent deserializeEvent(EventModel eventModel) {
        try {
            Class<?> eventClass = Class.forName("com.distributedinventory.cqrs.core.events." + eventModel.getEventType());
            return (BaseEvent) objectMapper.readValue(eventModel.getEventData(), eventClass);
        } catch (Exception e) {
            log.error("Error deserializing event: {}", e.getMessage());
            throw new RuntimeException("Error deserializing event", e);
        }
    }
}
