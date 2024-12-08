package com.example.event_ticketing.dto;

import java.util.List;

public class TicketPurchaseRequest {
    private Long eventId;
    private List<TicketPurchaseDetail> purchases;

    // Getters and setters
    public Long getEventId() {
        return eventId;
    }
    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public List<TicketPurchaseDetail> getPurchases() {
        return purchases;
    }
    public void setPurchases(List<TicketPurchaseDetail> purchases) {
        this.purchases = purchases;
    }
}