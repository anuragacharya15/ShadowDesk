package com.shadowfax.helpdesk.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a support ticket entity within Shadowfax's internal helpdesk.
 * Encapsulates ticket details, audit timestamps, and operational state.
 */
public class Ticket {

    public static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    public static final DateTimeFormatter STORAGE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final String ticketId;
    private String title;
    private String description;
    private TicketCategory category;
    private TicketPriority priority;
    private TicketStatus status;
    private final String createdBy;
    private String assignedTo;
    private String resolutionNotes;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Constructor used when creating a new ticket.
     */
    public Ticket(String ticketId, String title, String description,
                  TicketCategory category, TicketPriority priority,
                  String createdBy, String assignedTo) {
        this.ticketId = ticketId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.status = TicketStatus.OPEN;
        this.createdBy = createdBy;
        this.assignedTo = (assignedTo == null || assignedTo.trim().isEmpty()) ? "Unassigned" : assignedTo.trim();
        this.resolutionNotes = "";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    /**
     * Full constructor typically used when hydrating tickets from persistent storage.
     */
    public Ticket(String ticketId, String title, String description,
                  TicketCategory category, TicketPriority priority,
                  TicketStatus status, String createdBy, String assignedTo,
                  String resolutionNotes, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.ticketId = ticketId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.status = status;
        this.createdBy = createdBy;
        this.assignedTo = (assignedTo == null || assignedTo.trim().isEmpty()) ? "Unassigned" : assignedTo.trim();
        this.resolutionNotes = (resolutionNotes == null) ? "" : resolutionNotes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt != null ? updatedAt : createdAt;
    }

    public String getTicketId() {
        return ticketId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        touch();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        touch();
    }

    public TicketCategory getCategory() {
        return category;
    }

    public void setCategory(TicketCategory category) {
        this.category = category;
        touch();
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public void setPriority(TicketPriority priority) {
        this.priority = priority;
        touch();
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
        touch();
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = (assignedTo == null || assignedTo.trim().isEmpty()) ? "Unassigned" : assignedTo.trim();
        touch();
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
        touch();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Updates the last modified timestamp to current system time.
     */
    public void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Updates multiple editable fields in a single atomic operation.
     */
    public void updateDetails(String newTitle, String newDescription,
                              TicketCategory newCategory, TicketPriority newPriority,
                              String newAssignedTo) {
        if (newTitle != null && !newTitle.trim().isEmpty()) {
            this.title = newTitle.trim();
        }
        if (newDescription != null && !newDescription.trim().isEmpty()) {
            this.description = newDescription.trim();
        }
        if (newCategory != null) {
            this.category = newCategory;
        }
        if (newPriority != null) {
            this.priority = newPriority;
        }
        if (newAssignedTo != null) {
            this.assignedTo = newAssignedTo.trim().isEmpty() ? "Unassigned" : newAssignedTo.trim();
        }
        touch();
    }

    public String getFormattedCreatedAt() {
        return createdAt != null ? createdAt.format(DISPLAY_FORMATTER) : "N/A";
    }

    public String getFormattedUpdatedAt() {
        return updatedAt != null ? updatedAt.format(DISPLAY_FORMATTER) : "N/A";
    }

    @Override
    public String toString() {
        return "[" + ticketId + "] (" + priority + ") " + title + " [" + status + "]";
    }
}
