package com.distributedinventory.cqrs.core.handlers;

import com.distributedinventory.cqrs.core.domain.AggregateRoot;
import com.distributedinventory.cqrs.core.events.BaseEvent;
import com.distributedinventory.cqrs.core.events.EventModel;
import com.distributedinventory.cqrs.core.infrastructure.EventStore;
import com.distributedinventory.cqrs.core.producers.EventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventSourcingHandlerImpl<T extends AggregateRoot> implements EventSourcingHandler<T> {
    
    private final EventStore eventStore;
    private final EventProducer eventProducer;
    
    // Factory for creating aggregates - this would be more sophisticated in production
    private final Map<String, Supplier<T>> aggregateFactories = new ConcurrentHashMap<>();
    
    @Override
    public void save(AggregateRoot aggregate) {
        // Save events to event store
        eventStore.saveEvents(aggregate.getId(), aggregate.getUncommittedChanges(), aggregate.getVersion());
        
        // Publish events directly to Kafka (simplified for Phase 3)
        for (BaseEvent event : aggregate.getUncommittedChanges()) {
            String topic = determineTopic(event);
            eventProducer.produce(topic, event);
        }
        
        aggregate.markChangesAsCommitted();
        
        log.info("Saved and published events for aggregate: {} with {} events", 
                aggregate.getId(), aggregate.getUncommittedChanges().size());
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public T getById(String id) {
        List<BaseEvent> events = eventStore.getEvents(id);
        
        if (events.isEmpty()) {
            log.warn("No events found for aggregate: {}", id);
            return null;
        }
        
        log.info("Reconstructing aggregate: {} from {} events", id, events.size());
        
        // For now, return null to avoid casting issues
        // In production, you'd have proper aggregate reconstruction logic
        return null;
    }
    
    @Override
    public void republishEvents() {
        List<EventModel> eventModels = eventStore.findAll();
        
        log.info("Republishing {} events", eventModels.size());
        
        for (EventModel eventModel : eventModels) {
            try {
                // Deserialize and republish to Kafka
                BaseEvent event = deserializeEvent(eventModel);
                eventProducer.produce(eventModel.getEventType(), event);
                log.debug("Republished event: {} for aggregate: {}", 
                        eventModel.getEventType(), eventModel.getAggregateIdentifier());
            } catch (Exception e) {
                log.error("Error republishing event: {} - {}", eventModel.getId(), e.getMessage());
            }
        }
        
        log.info("Completed republishing {} events", eventModels.size());
    }
    
    private String determineTopic(BaseEvent event) {
        // Map event types to Kafka topics based on the OpenAPI spec
        String eventType = event.getClass().getSimpleName();
        
        switch (eventType) {
            case "ProductCreatedEvent":
            case "StockUpdatedEvent":
                return "stock-events";
            case "ReservationCreatedEvent":
            case "ReservationConfirmedEvent":
            case "ReservationCancelledEvent":
                return "reservation-events";
            case "TransferInitiatedEvent":
            case "TransferCompletedEvent":
            case "TransferFailedEvent":
                return "transfer-events";
            default:
                log.warn("Unknown event type: {}, using default topic", eventType);
                return "default-events";
        }
    }
    
    private BaseEvent deserializeEvent(EventModel eventModel) {
        // This would be implemented in the concrete implementation
        // For now, return null as this is just the interface
        return null;
    }
}
