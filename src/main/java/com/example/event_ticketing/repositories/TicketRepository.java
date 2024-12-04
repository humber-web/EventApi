package com.example.event_ticketing.repositories;

import com.example.event_ticketing.models.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    // Find available tickets by event ID and type
    List<Ticket> findByEventIdAndTypeAndStatus(Long eventId, String type, Ticket.TicketStatus status);

    // Find tickets by buyer's ID
    List<Ticket> findByBuyerId(Long buyerId);

    // Find tickets by event ID
    List<Ticket> findByEventId(Long eventId);
}
