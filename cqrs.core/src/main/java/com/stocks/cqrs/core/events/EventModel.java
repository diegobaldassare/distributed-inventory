package com.distributedinventory.cqrs.core.events;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "event_store")
public class EventModel {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @Column(name = "timestamp")
    private Date timeStamp;
    
    @Column(name = "aggregate_identifier")
    private String aggregateIdentifier;
    
    @Column(name = "aggregate_type")
    private String aggregateType;
    
    @Column(name = "version")
    private int version;
    
    @Column(name = "event_type")
    private String eventType;
    
    @Column(name = "event_data", columnDefinition = "TEXT")
    private String eventData;
}
