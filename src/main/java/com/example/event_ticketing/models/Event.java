package com.example.event_ticketing.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;

import java.util.HashSet;
import java.util.List;

import java.time.LocalDateTime;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Event name is required")
    private String name;

    private String description;

    @NotBlank(message = "Location is required")
    private String location;

    @FutureOrPresent(message = "Event date must be in the future")
    private LocalDateTime dateTime;

    @ManyToOne
    @JoinColumn(name = "organizer_id")
    private User organizer;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    private List<Ticket> tickets;

    @ManyToMany
    @JoinTable(name = "event_validators", joinColumns = @JoinColumn(name = "event_id"), inverseJoinColumns = @JoinColumn(name = "validator_id"))
    @JsonIgnore
    private Set<User> validators = new HashSet<>();

    private Double price;

    // Constructors
    public Event() {
    }

    public Event(String name, String description, LocalDateTime dateTime, String location, User organizer) {
        this.name = name;
        this.description = description;
        this.dateTime = dateTime;
        this.location = location;
        this.organizer = organizer;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public User getOrganizer() {
        return organizer;
    }

    public void setOrganizer(User organizer) {
        this.organizer = organizer;
    }

    public long getTicketsOfType(String type) {
        return tickets.stream()
                .filter(ticket -> ticket.getType().equals(type))
                .count();
    }

    // Method to get the price for a ticket type
    public Double getTicketPrice(String type) {
        // Here you could have different prices for different ticket types
        return this.price; // For simplicity, returning base price
    }
    public Set<User> getValidators() {
        return validators;
    }

    public void setValidators(Set<User> validators) {
        this.validators = validators;
    }
}
