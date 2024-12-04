package com.example.event_ticketing.controllers;

import com.example.event_ticketing.dto.TicketCreationRequest;
import com.example.event_ticketing.dto.TicketPurchaseRequest;
import com.example.event_ticketing.models.Event;
import com.example.event_ticketing.models.Ticket;
import com.example.event_ticketing.services.TicketService;
import com.example.event_ticketing.services.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Collections;

@RestController
@RequestMapping("/tickets")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private EventService eventService;

    // Get all tickets (Optional: Restrict to admins or organizers)
    @GetMapping
    public ResponseEntity<List<Ticket>> getAllTickets() {
        List<Ticket> tickets = ticketService.getAllTickets();
        return ResponseEntity.ok(tickets);
    }

    // Purchase a ticket
    @PostMapping("/{ticketId}/purchase")
    public ResponseEntity<Ticket> purchaseTicket(@PathVariable Long ticketId, Authentication authentication) {
        String userEmail = authentication.getName();
        Ticket purchasedTicket = ticketService.purchaseTicket(ticketId, userEmail);
        return ResponseEntity.ok(purchasedTicket);
    }

    /**
     * Endpoint for Organizers to create multiple tickets for an event.
     * Accessible only by users with the ORGANIZER role who are organizers of the
     * event.
     */
    @PostMapping("")
    public ResponseEntity<?> createTickets(
            @Valid @RequestBody TicketCreationRequest ticketRequest,
            Authentication authentication) {

        // Check if the authenticated user is the organizer of the event
        ResponseEntity<?> organizerCheckResponse = checkIfOrganizer(authentication, ticketRequest.getEventId());
        if (organizerCheckResponse != null) {
            return organizerCheckResponse;
        }

        // Proceed to create tickets
        List<Ticket> createdTickets = ticketService.createTickets(ticketRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTickets);
    }

    // Get a ticket by ID
    @GetMapping("/{id}")
    public ResponseEntity<Ticket> getTicketById(@PathVariable Long id) {
        Ticket ticket = ticketService.getTicketById(id);
        return ResponseEntity.ok(ticket);
    }

    /**
     * Endpoint for Users to purchase tickets.
     */
    @PostMapping("/purchase")
    public ResponseEntity<?> purchaseTickets(
            @Valid @RequestBody TicketPurchaseRequest purchaseRequest,
            Authentication authentication) {
        String userEmail = authentication.getName();
        List<Ticket> purchasedTickets = ticketService.purchaseTickets(purchaseRequest, userEmail);
        return ResponseEntity.ok(purchasedTickets);
    }

    /**
     * Endpoint for Organizers to view all tickets for their event.
     */
    @GetMapping("/event/{eventId}")
    public ResponseEntity<?> getTicketsForEvent(@PathVariable Long eventId, Authentication authentication) {
        // Check if the authenticated user is the organizer of the event
        ResponseEntity<?> organizerCheckResponse = checkIfOrganizer(authentication, eventId);
        if (organizerCheckResponse != null) {
            return organizerCheckResponse;
        }

        // Fetch and return the tickets for the event
        List<Ticket> tickets = ticketService.getTicketsByEventId(eventId);
        return ResponseEntity.ok(tickets);
    }

    /**
     * Endpoint for Users to view their purchased tickets.
     */
    @GetMapping("/my-tickets")
    public ResponseEntity<List<Ticket>> getMyTickets(Authentication authentication) {
        String userEmail = authentication.getName();
        List<Ticket> tickets = ticketService.getTicketsByBuyerEmail(userEmail);
        return ResponseEntity.ok(tickets);
    }

    /**
     * Endpoint for Organizers to validate a ticket.
     */
    @PostMapping("/{ticketId}/validate")
    public ResponseEntity<?> validateTicket(@PathVariable Long ticketId, Authentication authentication) {
        // Fetch the ticket details
        Ticket ticket = ticketService.getTicketById(ticketId);

        // Check if the authenticated user is the organizer of the event associated with the ticket
        String userEmail = authentication.getName();
        if (!ticket.getEvent().getOrganizer().getEmail().equals(userEmail)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Collections.singletonMap("error", "Access Denied: You are not the organizer of this event"));
        }

        // Proceed to validate the ticket
        ticketService.validateTicket(ticketId);
        return ResponseEntity.ok(Collections.singletonMap("message", "Ticket validated successfully"));
    }

    // Helper method to check if the authenticated user is the organizer of the event
    private ResponseEntity<?> checkIfOrganizer(Authentication authentication, Long eventId) {
        // Fetch the event details
        Event event = eventService.getEventById(eventId);

        // Check if the authenticated user is the organizer of the event
        String userEmail = authentication.getName();
        if (!event.getOrganizer().getEmail().equals(userEmail)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Collections.singletonMap("error", "Access Denied: You are not the organizer of this event"));
        }

        return null;
    }
}