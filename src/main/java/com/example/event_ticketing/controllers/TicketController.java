package com.example.event_ticketing.controllers;

import com.example.event_ticketing.models.Ticket;
import com.example.event_ticketing.repositories.TicketRepository;
import com.example.event_ticketing.repositories.EventRepository;
import com.example.event_ticketing.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/tickets")
public class TicketController {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    // GET all tickets
    @GetMapping
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    // GET ticket by ID
    @GetMapping("/{id}")
    public ResponseEntity<Ticket> getTicketById(@PathVariable Long id) {
        Optional<Ticket> ticketOptional = ticketRepository.findById(id);
        if (ticketOptional.isPresent()) {
            return ResponseEntity.ok(ticketOptional.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // POST create a new ticket
    @PostMapping
    public ResponseEntity<?> createTicket(@RequestBody Ticket ticket) {
        // Validate Event
        if (ticket.getEvent() == null || eventRepository.findById(ticket.getEvent().getId()).isEmpty()) {
            return ResponseEntity.badRequest().body("Event not found");
        }

        // Validate User
        if (ticket.getBuyer() == null || userRepository.findById(ticket.getBuyer().getId()).isEmpty()) {
            return ResponseEntity.badRequest().body("User not found");
        }

        // Save Ticket
        Ticket savedTicket = ticketRepository.save(ticket);
        return ResponseEntity.ok(savedTicket);
    }

    // PUT update an existing ticket
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTicket(@PathVariable Long id, @RequestBody Ticket updatedTicket) {
        Optional<Ticket> ticketOptional = ticketRepository.findById(id);

        if (ticketOptional.isPresent()) {
            Ticket ticket = ticketOptional.get();
            // Validate Event
            if (updatedTicket.getEvent() == null || eventRepository.findById(updatedTicket.getEvent().getId()).isEmpty()) {
                return ResponseEntity.badRequest().body("Event not found");
            }

            // Validate User
            if (updatedTicket.getBuyer() == null || userRepository.findById(updatedTicket.getBuyer().getId()).isEmpty()) {
                return ResponseEntity.badRequest().body("User not found");
            }

            ticket.setType(updatedTicket.getType());
            ticket.setPrice(updatedTicket.getPrice());
            ticket.setEvent(updatedTicket.getEvent());
            ticket.setBuyer(updatedTicket.getBuyer());
            return ResponseEntity.ok(ticketRepository.save(ticket));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE a ticket
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTicket(@PathVariable Long id) {
        Optional<Ticket> ticketOptional = ticketRepository.findById(id);

        if (ticketOptional.isPresent()) {
            ticketRepository.delete(ticketOptional.get());
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // GET tickets by user ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Ticket>> getTicketsByUser(@PathVariable Long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }
        List<Ticket> tickets = ticketRepository.findByBuyerId(userId);
        return ResponseEntity.ok(tickets);
    }
}