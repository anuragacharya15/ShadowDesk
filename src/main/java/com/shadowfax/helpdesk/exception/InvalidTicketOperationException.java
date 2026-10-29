package com.shadowfax.helpdesk.exception;

/**
 * Thrown when a business logic violation occurs on a ticket,
 * such as an invalid status transition or resolving without notes.
 */
public class InvalidTicketOperationException extends Exception {
    public InvalidTicketOperationException(String message) {
        super(message);
    }
}
