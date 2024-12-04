package com.example.event_ticketing.exceptions;

public class NotEnoughTicketsException extends RuntimeException {
    public NotEnoughTicketsException(String message) {
        super(message);
    }
}
