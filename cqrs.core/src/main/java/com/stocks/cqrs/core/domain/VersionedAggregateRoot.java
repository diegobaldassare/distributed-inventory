package com.distributedinventory.cqrs.core.domain;

import com.distributedinventory.cqrs.core.exceptions.ConcurrencyException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class VersionedAggregateRoot extends AggregateRoot {
    
    private final Logger logger = Logger.getLogger(VersionedAggregateRoot.class.getName());
    
    /**
     * Check if the current version matches the expected version
     * @param expectedVersion The version expected by the client
     * @return true if versions match, false otherwise
     */
    public boolean isVersionMatch(int expectedVersion) {
        return this.getVersion() == expectedVersion;
    }
    
    /**
     * Validate version for optimistic locking
     * @param expectedVersion The version expected by the client
     * @throws ConcurrencyException if versions don't match
     */
    public void validateVersion(int expectedVersion) {
        if (!isVersionMatch(expectedVersion)) {
            String message = MessageFormat.format(
                "Version mismatch. Expected: {0}, Current: {1}", 
                expectedVersion, this.getVersion()
            );
            logger.log(Level.WARNING, message);
            throw new ConcurrencyException(message);
        }
    }
    
    /**
     * Get the current version for optimistic locking responses
     * @return current version
     */
    public int getCurrentVersion() {
        return this.getVersion();
    }
}
