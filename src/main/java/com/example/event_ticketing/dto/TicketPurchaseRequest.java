package com.example.event_ticketing.dto;

import jakarta.validation.constraints.*;

public class TicketPurchaseRequest {

    @NotNull(message = "Event ID is required")
    private Long eventId;

    @NotBlank(message = "Ticket type is required")
    private String type;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;

    // Getters and Setters

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

}
