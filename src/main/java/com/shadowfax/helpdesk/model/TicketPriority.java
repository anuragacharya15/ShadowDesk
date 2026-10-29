package com.shadowfax.helpdesk.model;

/**
 * Represents the urgency level of a ticket.
 */
public enum TicketPriority {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High"),
    CRITICAL("Critical");

    private final String displayName;

    TicketPriority(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Resolves priority from enum name or display name.
     */
    public static TicketPriority fromString(String text) {
        if (text == null) {
            return null;
        }
        String trimmed = text.trim();
        for (TicketPriority priority : values()) {
            if (priority.name().equalsIgnoreCase(trimmed) || 
                priority.displayName.equalsIgnoreCase(trimmed)) {
                return priority;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
