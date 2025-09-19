package com.distributedinventory.cqrs.core.handlers;

import com.distributedinventory.cqrs.core.domain.AggregateRoot;

public interface EventSourcingHandler<T> {
    void save(AggregateRoot aggregate);
    T getById(String id);
    void republishEvents();
}