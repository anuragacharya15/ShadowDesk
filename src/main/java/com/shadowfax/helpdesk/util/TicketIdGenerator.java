package com.shadowfax.helpdesk.util;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utility for generating unique, formatted Ticket IDs (e.g., SF-1001, SF-1002).
 * Automatically initializes its counter from existing tickets so that
 * IDs are never repeated across restarts.
 */
public class TicketIdGenerator {

    private static final String PREFIX = "SF-";
    private static final int INITIAL_SEQUENCE = 1000;
    private final AtomicInteger currentSequence;

    public TicketIdGenerator() {
        this.currentSequence = new AtomicInteger(INITIAL_SEQUENCE);
    }

    /**
     * Inspects already loaded ticket IDs to ensure the generator resumes from the highest ID.
     */
    public synchronized void initializeFromExistingIds(Collection<String> existingIds) {
        if (existingIds == null || existingIds.isEmpty()) {
            return;
        }

        int maxIdFound = currentSequence.get();
        for (String id : existingIds) {
            if (id != null && id.startsWith(PREFIX)) {
                try {
                    String numberPart = id.substring(PREFIX.length()).trim();
                    int numericVal = Integer.parseInt(numberPart);
                    if (numericVal > maxIdFound) {
                        maxIdFound = numericVal;
                    }
                } catch (NumberFormatException ignored) {
                    // Ignore malformed custom IDs
                }
            }
        }
        currentSequence.set(maxIdFound);
    }

    /**
     * Generates the next sequential unique ticket ID.
     */
    public String generateNextId() {
        int nextVal = currentSequence.incrementAndGet();
        return PREFIX + nextVal;
    }

    /**
     * Returns the current sequence counter value for inspection or debugging.
     */
    public int getCurrentSequence() {
        return currentSequence.get();
    }
}
