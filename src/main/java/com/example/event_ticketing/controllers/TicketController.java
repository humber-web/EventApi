package com.example.event_ticketing.controllers;

import java.io.IOException;
import com.example.event_ticketing.dto.TicketCreationRequest;
import com.example.event_ticketing.models.User;
import com.example.event_ticketing.dto.TicketPurchaseRequest;
import com.example.event_ticketing.models.Event;
import com.example.event_ticketing.models.Ticket;
import com.example.event_ticketing.services.TicketService;
import com.example.event_ticketing.services.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.example.event_ticketing.repositories.UserRepository;
import com.example.event_ticketing.exceptions.TicketAlreadyValidatedException;
import com.example.event_ticketing.exceptions.TicketNotFoundException;
import com.google.zxing.WriterException;
import com.example.event_ticketing.exceptions.UserNotFoundException;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Collections;

import java.util.Map;

@RestController
@RequestMapping("/tickets")
public class TicketController {

    @Autowired
    private UserRepository userRepository;

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
            Authentication authentication) 
            throws IOException, WriterException {
    
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

        try {
            Map<String, Object> purchaseResult = ticketService.purchaseTickets(purchaseRequest, userEmail);
            return ResponseEntity.status(HttpStatus.CREATED).body(purchaseResult);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
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
        String userEmail = authentication.getName();

        // Check if the user has VALIDATOR or ADMIN role
        ResponseEntity<?> roleCheckResponse = checkIfValidatorOrAdmin(authentication);
        if (roleCheckResponse != null) {
            return roleCheckResponse;
        }

        // Fetch the user performing the validation
        User validator = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + userEmail));

        // Call the service method with ticketId and validatorId
        Ticket validatedTicket = ticketService.validateTicket(ticketId, validator.getId());

        return ResponseEntity.ok(validatedTicket);
    }

    // Helper method to check if the authenticated user is the organizer of the
    // event
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

    private ResponseEntity<?> checkIfValidatorOrAdmin(Authentication authentication) {
        // Fetch the user details
        User user = userRepository.findByEmail(authentication.getName())
                .orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "User is not authenticated"));
        }

        // Check for VALIDATOR or ADMIN roles
        if (user.getRole() != User.Role.VALIDATOR && user.getRole() != User.Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Collections.singletonMap("error",
                            "Access Denied: You do not have permission to validate tickets"));
        }

        return null; // User has proper role; proceed with operation
    }
}