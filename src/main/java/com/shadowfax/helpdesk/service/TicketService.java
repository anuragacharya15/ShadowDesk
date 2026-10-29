package com.shadowfax.helpdesk.service;

import com.shadowfax.helpdesk.exception.InvalidTicketOperationException;
import com.shadowfax.helpdesk.exception.TicketNotFoundException;
import com.shadowfax.helpdesk.model.*;
import com.shadowfax.helpdesk.repository.TicketRepository;
import com.shadowfax.helpdesk.util.TicketIdGenerator;

import java.util.*;

/**
 * Core business service managing ticket lifecycles, operations, search, and metrics.
 * Enforces business rules and prevents inconsistent states.
 */
public class TicketService {

    private final TicketRepository repository;
    private final TicketIdGenerator idGenerator;
    private final Map<String, Ticket> ticketsMap;

    public TicketService(TicketRepository repository, TicketIdGenerator idGenerator) {
        this.repository = repository;
        this.idGenerator = idGenerator;
        this.ticketsMap = new LinkedHashMap<>();
        loadData();
    }

    /**
     * Initializes tickets from the persistent repository and synchronizes the ID generator.
     */
    public synchronized void loadData() {
        ticketsMap.clear();
        Map<String, Ticket> loaded = repository.loadAll();
        ticketsMap.putAll(loaded);
        idGenerator.initializeFromExistingIds(ticketsMap.keySet());
    }

    /**
     * Persists all active tickets to storage.
     */
    public synchronized boolean saveData() {
        return repository.saveAll(ticketsMap.values());
    }

    /**
     * Creates a new ticket after validating all required inputs.
     * Starts with OPEN status and generated unique ID.
     */
    public synchronized Ticket createTicket(String title, String description,
                                           TicketCategory category, TicketPriority priority,
                                           String createdBy, String assignedTo) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Ticket title cannot be empty or blank.");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Ticket description cannot be empty or blank.");
        }
        if (category == null) {
            throw new IllegalArgumentException("Ticket category must be specified.");
        }
        if (priority == null) {
            throw new IllegalArgumentException("Ticket priority must be specified.");
        }
        if (createdBy == null || createdBy.trim().isEmpty()) {
            throw new IllegalArgumentException("Creator name/ID cannot be empty or blank.");
        }

        String ticketId = idGenerator.generateNextId();
        // Defensive check to absolutely guarantee uniqueness
        while (ticketsMap.containsKey(ticketId)) {
            ticketId = idGenerator.generateNextId();
        }

        Ticket ticket = new Ticket(
                ticketId,
                title.trim(),
                description.trim(),
                category,
                priority,
                createdBy.trim(),
                assignedTo != null ? assignedTo.trim() : null
        );

        ticketsMap.put(ticketId, ticket);
        return ticket;
    }

    /**
     * Retrieves all tickets currently registered in the system.
     */
    public synchronized List<Ticket> getAllTickets() {
        return new ArrayList<>(ticketsMap.values());
    }

    /**
     * Retrieves a single ticket by its ID (case-insensitive).
     * Throws TicketNotFoundException if no matching ticket exists.
     */
    public synchronized Ticket getTicketById(String ticketId) throws TicketNotFoundException {
        if (ticketId == null || ticketId.trim().isEmpty()) {
            throw new TicketNotFoundException("Ticket ID cannot be empty.");
        }
        String normalizedId = ticketId.trim().toUpperCase();
        Ticket ticket = ticketsMap.get(normalizedId);
        if (ticket == null) {
            // Also check case-insensitive just in case custom IDs exist
            for (Map.Entry<String, Ticket> entry : ticketsMap.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(normalizedId)) {
                    return entry.getValue();
                }
            }
            throw new TicketNotFoundException("Ticket with ID '" + ticketId.trim() + "' does not exist.");
        }
        return ticket;
    }

    /**
     * Updates editable details of an existing ticket.
     * Ignores null or empty values when preserving existing content.
     */
    public synchronized Ticket updateTicketDetails(String ticketId, String newTitle, String newDescription,
                                                   TicketCategory newCategory, TicketPriority newPriority,
                                                   String newAssignedTo) throws TicketNotFoundException {
        Ticket ticket = getTicketById(ticketId);

        if (newTitle != null && newTitle.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be set to a blank value.");
        }
        if (newDescription != null && newDescription.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be set to a blank value.");
        }

        ticket.updateDetails(newTitle, newDescription, newCategory, newPriority, newAssignedTo);
        return ticket;
    }

    /**
     * Returns the list of permitted next statuses for a given current status.
     */
    public List<TicketStatus> getAvailableTransitions(TicketStatus currentStatus) {
        List<TicketStatus> transitions = new ArrayList<>();
        if (currentStatus == null) {
            return transitions;
        }

        switch (currentStatus) {
            case OPEN:
                transitions.add(TicketStatus.IN_PROGRESS);
                transitions.add(TicketStatus.RESOLVED);
                break;
            case IN_PROGRESS:
                transitions.add(TicketStatus.RESOLVED);
                break;
            case RESOLVED:
                transitions.add(TicketStatus.CLOSED);
                transitions.add(TicketStatus.OPEN);
                break;
            case CLOSED:
                transitions.add(TicketStatus.OPEN);
                break;
        }
        return transitions;
    }

    /**
     * Changes a ticket's status adhering strictly to the lifecycle state machine.
     */
    public synchronized Ticket changeStatus(String ticketId, TicketStatus newStatus)
            throws TicketNotFoundException, InvalidTicketOperationException {
        Ticket ticket = getTicketById(ticketId);

        if (newStatus == null) {
            throw new InvalidTicketOperationException("Target status cannot be null.");
        }

        if (ticket.getStatus() == newStatus) {
            throw new InvalidTicketOperationException("Ticket " + ticketId + " is already in status " + newStatus.getDisplayName() + ".");
        }

        List<TicketStatus> allowedTransitions = getAvailableTransitions(ticket.getStatus());
        if (!allowedTransitions.contains(newStatus)) {
            throw new InvalidTicketOperationException(
                    "Invalid status transition: Cannot move ticket from " +
                            ticket.getStatus().getDisplayName() + " to " + newStatus.getDisplayName() +
                            ". Allowed transitions: " + formatAllowedTransitions(allowedTransitions)
            );
        }

        if (newStatus == TicketStatus.RESOLVED) {
            throw new InvalidTicketOperationException("To resolve a ticket, resolution notes must be provided. Please use the Resolve Ticket option.");
        }

        ticket.setStatus(newStatus);
        ticket.touch();
        return ticket;
    }

    /**
     * Resolves a ticket. Requires non-empty resolution notes.
     */
    public synchronized Ticket resolveTicket(String ticketId, String resolutionNotes)
            throws TicketNotFoundException, InvalidTicketOperationException {
        Ticket ticket = getTicketById(ticketId);

        if (ticket.getStatus() == TicketStatus.CLOSED) {
            throw new InvalidTicketOperationException("Ticket " + ticketId + " is CLOSED. Reopen it before resolving.");
        }
        if (ticket.getStatus() == TicketStatus.RESOLVED) {
            throw new InvalidTicketOperationException("Ticket " + ticketId + " is already RESOLVED.");
        }
        if (resolutionNotes == null || resolutionNotes.trim().isEmpty()) {
            throw new InvalidTicketOperationException("Resolution notes are mandatory and cannot be blank.");
        }

        ticket.setResolutionNotes(resolutionNotes.trim());
        ticket.setStatus(TicketStatus.RESOLVED);
        ticket.touch();
        return ticket;
    }

    /**
     * Closes a ticket. Strict rule: Only RESOLVED tickets can be closed.
     */
    public synchronized Ticket closeTicket(String ticketId)
            throws TicketNotFoundException, InvalidTicketOperationException {
        Ticket ticket = getTicketById(ticketId);

        if (ticket.getStatus() != TicketStatus.RESOLVED) {
            throw new InvalidTicketOperationException(
                    "Cannot close ticket " + ticketId + ". A ticket must be in RESOLVED status before it can be closed. " +
                            "Current status is: " + ticket.getStatus().getDisplayName() + "."
            );
        }

        ticket.setStatus(TicketStatus.CLOSED);
        ticket.touch();
        return ticket;
    }

    /**
     * Reopens a RESOLVED or CLOSED ticket, moving it back to OPEN.
     * Retains historical resolution notes with an audit note.
     */
    public synchronized Ticket reopenTicket(String ticketId, String reopenReason)
            throws TicketNotFoundException, InvalidTicketOperationException {
        Ticket ticket = getTicketById(ticketId);

        if (ticket.getStatus() != TicketStatus.RESOLVED && ticket.getStatus() != TicketStatus.CLOSED) {
            throw new InvalidTicketOperationException(
                    "Only RESOLVED or CLOSED tickets can be reopened. Ticket " + ticketId + " is currently " + ticket.getStatus().getDisplayName() + "."
            );
        }

        String existingNotes = ticket.getResolutionNotes();
        StringBuilder auditNotes = new StringBuilder();
        if (existingNotes != null && !existingNotes.trim().isEmpty()) {
            auditNotes.append("[Previous Resolution: ").append(existingNotes).append("] ");
        }
        if (reopenReason != null && !reopenReason.trim().isEmpty()) {
            auditNotes.append("[Reopen Reason: ").append(reopenReason.trim()).append("]");
        }
        ticket.setResolutionNotes(auditNotes.toString().trim());

        ticket.setStatus(TicketStatus.OPEN);
        ticket.touch();
        return ticket;
    }

    /**
     * Filters tickets according to any combination of status, priority, category, or creator.
     * Passing null or blank skips that filter criterion.
     */
    public synchronized List<Ticket> filterTickets(TicketStatus status, TicketPriority priority,
                                                  TicketCategory category, String creator) {
        List<Ticket> results = new ArrayList<>();
        String normalizedCreator = (creator != null && !creator.trim().isEmpty()) ? creator.trim().toLowerCase() : null;

        for (Ticket t : ticketsMap.values()) {
            if (status != null && t.getStatus() != status) {
                continue;
            }
            if (priority != null && t.getPriority() != priority) {
                continue;
            }
            if (category != null && t.getCategory() != category) {
                continue;
            }
            if (normalizedCreator != null && !t.getCreatedBy().toLowerCase().contains(normalizedCreator)) {
                continue;
            }
            results.add(t);
        }
        return results;
    }

    /**
     * Computes dashboard statistics dynamically from the active in-memory collection.
     */
    public synchronized DashboardStats getDashboardStatistics() {
        int total = ticketsMap.size();
        int openCount = 0;
        int inProgressCount = 0;
        int resolvedCount = 0;
        int closedCount = 0;
        int highPriority = 0;
        int criticalPriority = 0;

        for (Ticket t : ticketsMap.values()) {
            switch (t.getStatus()) {
                case OPEN:
                    openCount++;
                    break;
                case IN_PROGRESS:
                    inProgressCount++;
                    break;
                case RESOLVED:
                    resolvedCount++;
                    break;
                case CLOSED:
                    closedCount++;
                    break;
            }

            if (t.getPriority() == TicketPriority.HIGH) {
                highPriority++;
            } else if (t.getPriority() == TicketPriority.CRITICAL) {
                criticalPriority++;
            }
        }

        return new DashboardStats(total, openCount, inProgressCount, resolvedCount, closedCount, highPriority, criticalPriority);
    }

    private String formatAllowedTransitions(List<TicketStatus> list) {
        if (list.isEmpty()) {
            return "None (Terminal state until reopened)";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i).getDisplayName());
            if (i < list.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}
