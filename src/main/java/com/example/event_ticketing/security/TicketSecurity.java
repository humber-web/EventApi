package com.example.event_ticketing.security;

import com.example.event_ticketing.models.Ticket;
import com.example.event_ticketing.services.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("ticketSecurity")
public class TicketSecurity {

    @Autowired
    private TicketService ticketService;

    /**
     * Checks if the authenticated user can validate the specified ticket.
     * Only the organizer of the event associated with the ticket can validate it.
     *
     * @param authentication The authentication object containing user details.
     * @param ticketId       The ID of the ticket to validate.
     * @return true if the user can validate the ticket, false otherwise.
     */
    public boolean canValidateTicket(Authentication authentication, Long ticketId) {
        String email = authentication.getName();
        Ticket ticket = ticketService.getTicketById(ticketId);
        String organizerEmail = ticket.getEvent().getOrganizer().getEmail();
        return email.equals(organizerEmail);
    }
}