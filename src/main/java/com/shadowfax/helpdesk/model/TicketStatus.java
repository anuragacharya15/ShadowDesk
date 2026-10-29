package com.shadowfax.helpdesk.model;

/**
 * Represents the lifecycle state of a ticket.
 */
public enum TicketStatus {
    OPEN("Open"),
    IN_PROGRESS("In Progress"),
    RESOLVED("Resolved"),
    CLOSED("Closed");

    private final String displayName;

    TicketStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static TicketStatus fromString(String text) {
        if (text == null) {
            return null;
        }
        String trimmed = text.trim();
        for (TicketStatus status : values()) {
            if (status.name().equalsIgnoreCase(trimmed) || 
                status.displayName.equalsIgnoreCase(trimmed)) {
                return status;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
