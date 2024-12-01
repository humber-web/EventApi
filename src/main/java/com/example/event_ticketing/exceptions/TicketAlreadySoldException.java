package com.example.event_ticketing.exceptions;

public class TicketAlreadySoldException extends RuntimeException {
    public TicketAlreadySoldException(String message) {
        super(message);
    }
}
