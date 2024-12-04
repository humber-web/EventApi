package com.example.event_ticketing.controllers;

import com.example.event_ticketing.models.Event;
import com.example.event_ticketing.services.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/events")
public class EventController {

    @Autowired
    private EventService eventService;

    // Get all events (public)
    @GetMapping
    public ResponseEntity<List<Event>> getAllEvents() {
        List<Event> events = eventService.getAllEvents();
        return ResponseEntity.ok(events);
    }

    // Create a new event (Organizers only)
    @PreAuthorize("hasRole('ORGANIZER')")
    @PostMapping
    public ResponseEntity<Event> createEvent(@Valid @RequestBody Event event, Authentication authentication) {
        String userEmail = authentication.getName();
        Event createdEvent = eventService.createEvent(event, userEmail);
        return ResponseEntity.status(201).body(createdEvent);
    }

    // Get an event by ID (public)
    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        Event event = eventService.getEventById(id);
        return ResponseEntity.ok(event);
    }

    // Update an event (Organizers only)
    @PreAuthorize("hasRole('ROLE_ORGANIZER') or hasRole('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Event> updateEvent(@PathVariable Long id, @Valid @RequestBody Event updatedEvent, Authentication authentication) {
        String userEmail = authentication.getName();
        Event event = eventService.updateEvent(id, updatedEvent, userEmail);
        return ResponseEntity.ok(event);
    }

    // Delete an event (Organizers only)
    @PreAuthorize("hasRole('ROLE_ORGANIZER') or hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id, Authentication authentication) {
        String userEmail = authentication.getName();
        eventService.deleteEvent(id, userEmail);
        return ResponseEntity.noContent().build();
    }
}
