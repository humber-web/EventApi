package com.example.event_ticketing.dto;

public class TicketPurchaseDetail {
    private String type;
    private int quantity;

    // Getters and setters
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}