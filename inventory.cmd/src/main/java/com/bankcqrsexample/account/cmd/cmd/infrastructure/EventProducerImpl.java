package com.bankcqrsexample.account.cmd.cmd.infrastructure;

import com.distributedinventory.cqrs.core.events.BaseEvent;
import com.distributedinventory.cqrs.core.events.EventModel;
import com.distributedinventory.cqrs.core.producers.EventProducer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventProducerImpl implements EventProducer {
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    @Override
    public void produce(String topic, BaseEvent event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            String key = extractKey(event);
            
            log.info("Publishing event to topic: {} with key: {}", topic, key);
            
            CompletableFuture<SendResult<String, String>> future = 
                kafkaTemplate.send(topic, key, eventJson);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Successfully published event to topic: {} with offset: {}", 
                            topic, result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to publish event to topic: {} - {}", topic, ex.getMessage());
                    // In production, you might want to implement retry logic or dead letter queue
                }
            });
            
        } catch (JsonProcessingException e) {
            log.error("Error serializing event for topic: {} - {}", topic, e.getMessage());
            throw new RuntimeException("Error serializing event", e);
        }
    }
    
    @Override
    public void produce(String topic, EventModel eventModel) {
        try {
            String eventJson = eventModel.getEventData();
            String key = eventModel.getAggregateIdentifier();
            
            log.info("Publishing event model to topic: {} with key: {}", topic, key);
            
            CompletableFuture<SendResult<String, String>> future = 
                kafkaTemplate.send(topic, key, eventJson);
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Successfully published event model to topic: {} with offset: {}", 
                            topic, result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to publish event model to topic: {} - {}", topic, ex.getMessage());
                }
            });
            
        } catch (Exception e) {
            log.error("Error publishing event model to topic: {} - {}", topic, e.getMessage());
            throw new RuntimeException("Error publishing event model", e);
        }
    }
    
    private String extractKey(Object event) {
        // Extract key from event - this is a simplified implementation
        // In production, you'd have proper key extraction logic
        if (event instanceof EventModel) {
            return ((EventModel) event).getAggregateIdentifier();
        }
        
        if (event instanceof BaseEvent) {
            return ((BaseEvent) event).getId();
        }
        
        // For other events, try to extract ID field
        try {
            return objectMapper.readTree(objectMapper.writeValueAsString(event))
                    .get("id").asText();
        } catch (Exception e) {
            log.warn("Could not extract key from event, using default");
            return "default-key";
        }
    }
}
