package com.example.event_ticketing.controllers;

import com.example.event_ticketing.models.Ticket;
import com.example.event_ticketing.services.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

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

    // Get tickets purchased by the current user
    @GetMapping("/my-tickets")
    public ResponseEntity<List<Ticket>> getMyTickets(Authentication authentication) {
        String userEmail = authentication.getName();
        List<Ticket> tickets = ticketService.getTicketsByBuyerEmail(userEmail);
        return ResponseEntity.ok(tickets);
    }

    // Other endpoints as needed...
}
