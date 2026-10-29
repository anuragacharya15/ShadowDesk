package com.shadowfax.helpdesk.model;

/**
 * Represents a user in the Shadowfax helpdesk system.
 */
public class User {
    private final String userId;
    private final String name;
    private final UserRole role;

    public User(String userId, String name, UserRole role) {
        this.userId = userId;
        this.name = name;
        this.role = role;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public UserRole getRole() {
        return role;
    }

    @Override
    public String toString() {
        return name + " (" + role.getDisplayName() + ")";
    }
}
