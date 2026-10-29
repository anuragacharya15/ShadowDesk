package com.shadowfax.helpdesk.model;

/**
 * Basic roles supported in the internal helpdesk system.
 */
public enum UserRole {
    EMPLOYEE("Employee / Requester"),
    SUPPORT_AGENT("Support Agent");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
