package com.example.event_ticketing.services;

import com.example.event_ticketing.models.Ticket;
import com.example.event_ticketing.models.User;
import com.example.event_ticketing.models.Event; // Ensure this import is present
import com.example.event_ticketing.models.Ticket.TicketStatus;
import com.example.event_ticketing.repositories.TicketRepository;
import com.example.event_ticketing.repositories.UserRepository;
import com.example.event_ticketing.exceptions.TicketNotFoundException;
import com.example.event_ticketing.dto.TicketCreationRequest;
import com.example.event_ticketing.dto.TicketPurchaseRequest;
import com.example.event_ticketing.exceptions.TicketAlreadySoldException;
import com.example.event_ticketing.exceptions.TicketAlreadyValidatedException;
import com.example.event_ticketing.exceptions.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.event_ticketing.exceptions.EventNotFoundException;
import com.example.event_ticketing.exceptions.NotEnoughTicketsException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventService eventService;

    // Purchase a ticket
    public Ticket purchaseTicket(Long ticketId, String userEmail) {
        // Find the ticket by ID
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found with ID: " + ticketId));

        // Check if the ticket is available
        if (ticket.getStatus() == TicketStatus.SOLD) {
            throw new TicketAlreadySoldException("Ticket with ID " + ticketId + " has already been sold");
        }

        // Find the buyer by email
        User buyer = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + userEmail));

        // Simulate payment processing here (for now, we assume payment is successful)

        // Update ticket status and assign buyer
        ticket.setStatus(TicketStatus.SOLD);
        ticket.setBuyer(buyer);

        // Save the updated ticket
        return ticketRepository.save(ticket);
    }

    // Get all tickets (optional)
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    // Get tickets purchased by a user
    public List<Ticket> getTicketsByBuyerEmail(String userEmail) {
        User buyer = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + userEmail));

        return ticketRepository.findByBuyerId(buyer.getId());
    }

    // Create multiple tickets (Organizer)
    public List<Ticket> createTickets(TicketCreationRequest ticketRequest) {
        Event event = eventService.getEventById(ticketRequest.getEventId());

        List<Ticket> tickets = new ArrayList<>();
        for (int i = 0; i < ticketRequest.getQuantity(); i++) {
            Ticket ticket = new Ticket();
            ticket.setType(ticketRequest.getType());
            ticket.setPrice(ticketRequest.getPrice());
            ticket.setEvent(event);
            ticket.setStatus(Ticket.TicketStatus.AVAILABLE);
            ticket.setQrCodeData(UUID.randomUUID().toString()); // Generate unique QR code data

            tickets.add(ticket);
        }

        return ticketRepository.saveAll(tickets);
    }

    // Purchase multiple tickets
    public List<Ticket> purchaseTickets(TicketPurchaseRequest purchaseRequest, String userEmail) {
        Event event = eventService.getEventById(purchaseRequest.getEventId());

        User buyer = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + userEmail));

        // Fetch available tickets for the event and type
        List<Ticket> availableTickets = ticketRepository.findByEventIdAndTypeAndStatus(
                event.getId(),
                purchaseRequest.getType(),
                Ticket.TicketStatus.AVAILABLE);

        if (availableTickets.size() < purchaseRequest.getQuantity()) {
            throw new NotEnoughTicketsException("Not enough tickets available for the requested quantity");
        }

        List<Ticket> ticketsToPurchase = availableTickets.subList(0, purchaseRequest.getQuantity());

        // Update tickets with buyer information and status
        for (Ticket ticket : ticketsToPurchase) {
            ticket.setBuyer(buyer);
            ticket.setStatus(Ticket.TicketStatus.SOLD);
            // Optionally, assign QR code data if not already set
            if (ticket.getQrCodeData() == null || ticket.getQrCodeData().isEmpty()) {
                ticket.setQrCodeData(UUID.randomUUID().toString());
            }
        }

        // Save the updated tickets
        return ticketRepository.saveAll(ticketsToPurchase);
    }

    // Get a ticket by ID
    public Ticket getTicketById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found with ID: " + id));
    }

    public List<Ticket> getTicketsByEventId(Long eventId) {
        Event event = eventService.getEventById(eventId);
        if (event == null) {
            throw new EventNotFoundException("Event not found with ID: " + eventId);
        }
        return ticketRepository.findByEventId(eventId);
    }

    // Validate a ticket
    public Ticket validateTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found with ID: " + ticketId));

        if (ticket.getStatus() == TicketStatus.VALIDATED) {
            throw new TicketAlreadyValidatedException("Ticket with ID " + ticketId + " has already been validated");
        }

        ticket.setStatus(TicketStatus.VALIDATED);
        return ticketRepository.save(ticket);
    }

    // Update a ticket
    public Ticket updateTicket(Long id, Ticket updatedTicket) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found with ID: " + id));

        // Update ticket details
        ticket.setEvent(updatedTicket.getEvent());
        ticket.setPrice(updatedTicket.getPrice());
        ticket.setStatus(updatedTicket.getStatus());
        ticket.setBuyer(updatedTicket.getBuyer());

        return ticketRepository.save(ticket);
    }

    // Delete a ticket
    public void deleteTicket(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found with ID: " + id));

        ticketRepository.delete(ticket);
    }
}