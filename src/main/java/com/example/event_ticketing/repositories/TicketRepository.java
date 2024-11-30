package com.example.event_ticketing.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.event_ticketing.models.Ticket;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByBuyerId(Long buyerId);
    List<Ticket> findByEventId(Long eventId);

}
