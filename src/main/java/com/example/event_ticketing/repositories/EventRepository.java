package com.example.event_ticketing.repositories;

import com.example.event_ticketing.models.Event;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByOrganizerId(Long organizerId);
    List<Event> findByDateTimeAfter(LocalDateTime dateTime);

}
