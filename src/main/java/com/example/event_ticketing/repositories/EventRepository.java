package com.example.event_ticketing.repositories;

import com.example.event_ticketing.models.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {
}
