package com.shadowfax.helpdesk.model;

/**
 * Represents the functional business category for a support ticket at Shadowfax.
 */
public enum TicketCategory {
    TECHNICAL("Technical Support"),
    DELIVERY_OPERATIONS("Delivery & Logistics Operations"),
    ACCOUNT_ACCESS("Account & Access Management"),
    HARDWARE("Hardware & Device Support"),
    OTHER("Other / General Inquiries");

    private final String displayName;

    TicketCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Resolves a category from a string matching name or display name.
     */
    public static TicketCategory fromString(String text) {
        if (text == null) {
            return null;
        }
        String trimmed = text.trim();
        for (TicketCategory category : values()) {
            if (category.name().equalsIgnoreCase(trimmed) || 
                category.displayName.equalsIgnoreCase(trimmed)) {
                return category;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
