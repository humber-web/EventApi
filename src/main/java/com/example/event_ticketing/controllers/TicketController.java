package com.example.event_ticketing.controllers;

import com.example.event_ticketing.dto.TicketCreationRequest;
import com.example.event_ticketing.dto.TicketPurchaseRequest;
import com.example.event_ticketing.models.Ticket;
import com.example.event_ticketing.services.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
     * Accessible only by users with the ORGANIZER role who are organizers of the event.
     */
    @PreAuthorize("hasRole('ORGANIZER') and @eventSecurity.isOrganizer(authentication, #ticketRequest.eventId)")
    @PostMapping("")
    public ResponseEntity<List<Ticket>> createTickets(
            @Valid @RequestBody TicketCreationRequest ticketRequest,
            Authentication authentication) {
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
    @PreAuthorize("hasRole('ATTENDEE') or hasRole('ORGANIZER') or hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ORGANIZER') and @eventSecurity.isOrganizer(authentication, #eventId)")
    @GetMapping("/event/{eventId}")
    public ResponseEntity<?> getTicketsForEvent(@PathVariable Long eventId) {
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
    @PreAuthorize("hasRole('ORGANIZER') and @ticketSecurity.canValidateTicket(authentication, #ticketId)")
    @PostMapping("/{ticketId}/validate")
    public ResponseEntity<?> validateTicket(@PathVariable Long ticketId) {
        ticketService.validateTicket(ticketId);
        return ResponseEntity.ok(Collections.singletonMap("message", "Ticket validated successfully"));
    }

    // Update a ticket
    @PutMapping("/{id}")
    public ResponseEntity<Ticket> updateTicket(@PathVariable Long id, @Valid @RequestBody Ticket updatedTicket) {
        Ticket ticket = ticketService.updateTicket(id, updatedTicket);
        return ResponseEntity.ok(ticket);
    }

    // Delete a ticket
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicket(@PathVariable Long id) {
        ticketService.deleteTicket(id);
        return ResponseEntity.noContent().build();
    }
}
