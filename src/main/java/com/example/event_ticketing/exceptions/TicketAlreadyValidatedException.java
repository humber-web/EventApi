package com.example.event_ticketing.exceptions;

public class TicketAlreadyValidatedException extends RuntimeException {
    public TicketAlreadyValidatedException(String message) {
        super(message);
    }
}
