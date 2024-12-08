package com.example.event_ticketing.dto;


import java.math.BigDecimal;

import com.example.event_ticketing.models.Ticket;

public class TicketDTO {
    private Long id;
    private String buyerEmail;
    private String eventName;
    private Double price;
    private String qrCodeData; // Add QR Code Data field

    // Constructor
    public TicketDTO(Long id, String buyerEmail, String eventName, Double price, String qrCodeData) {
        this.id = id;
        this.buyerEmail = buyerEmail;
        this.eventName = eventName;
        this.price = price;
        this.qrCodeData = qrCodeData;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBuyerEmail() { return buyerEmail; }
    public void setBuyerEmail(String buyerEmail) { this.buyerEmail = buyerEmail; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getQrCodeData() { return qrCodeData; }
    public void setQrCodeData(String qrCodeData) { this.qrCodeData = qrCodeData; }
}
