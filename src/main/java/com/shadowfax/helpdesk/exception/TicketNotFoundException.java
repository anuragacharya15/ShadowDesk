package com.shadowfax.helpdesk.exception;

/**
 * Thrown when an operation targets a ticket ID that does not exist in the system.
 */
public class TicketNotFoundException extends Exception {
    public TicketNotFoundException(String message) {
        super(message);
    }
}
