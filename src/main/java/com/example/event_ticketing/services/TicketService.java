package com.example.event_ticketing.services;

import com.example.event_ticketing.models.Ticket;
import com.example.event_ticketing.models.User;
import com.example.event_ticketing.models.Ticket.TicketStatus;
import com.example.event_ticketing.repositories.TicketRepository;
import com.example.event_ticketing.repositories.UserRepository;
import com.example.event_ticketing.exceptions.TicketNotFoundException;
import com.example.event_ticketing.exceptions.TicketAlreadySoldException;
import com.example.event_ticketing.exceptions.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

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

    // Additional methods as needed...
}
