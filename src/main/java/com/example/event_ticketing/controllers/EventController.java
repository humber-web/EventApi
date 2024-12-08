package com.example.event_ticketing.controllers;

import com.example.event_ticketing.models.Event;
import com.example.event_ticketing.models.User;
import com.example.event_ticketing.services.EventService;
import com.example.event_ticketing.repositories.UserRepository;
import com.example.event_ticketing.exceptions.UserNotFoundException;
import com.example.event_ticketing.exceptions.EventNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/events")
public class EventController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventService eventService;

    // Other endpoints...

    /**
     * Endpoint for Organizers to assign a validator to their event.
     */
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    @PostMapping("/{eventId}/assign-validator")
    public ResponseEntity<?> assignValidatorToEvent(
            @PathVariable Long eventId,
            @RequestParam Long validatorId,
            Authentication authentication) {

        String organizerEmail = authentication.getName();

        try {
            // Assign validator to event
            eventService.assignValidatorToEvent(eventId, validatorId, organizerEmail);
            return ResponseEntity.ok(Collections.singletonMap("message", "Validator assigned to event successfully"));
        } catch (UserNotFoundException | EventNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("error", e.getMessage()));
        } catch (Exception e) {
            // For any other unforeseen exceptions
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "An unexpected error occurred"));
        }
    }

    /**
     * Helper method to check if the authenticated user is the organizer of the event.
     * Returns a ResponseEntity with an error if the user is not the organizer, otherwise null.
     */
    private ResponseEntity<?> checkIfOrganizer(Authentication authentication, Long eventId) {
        String userEmail = authentication.getName();
        Event event = eventService.getEventById(eventId);
        
        if (event == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("error", "Event not found with ID: " + eventId));
        }

        if (!event.getOrganizer().getEmail().equals(userEmail) && 
            !hasRole(authentication, "ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Collections.singletonMap("error", "Access Denied: You are not the organizer of this event"));
        }
        return null;
    }

    /**
     * Utility method to check if the user has a specific role.
     */
    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }

    // Existing methods like getAllTickets, createTickets, deleteEvent, etc.

    /**
     * Example of another method updateEvent with organizer check.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody Event updatedEvent,
            Authentication authentication) {
        
        // Check if the authenticated user is the organizer of the event
        ResponseEntity<?> organizerCheckResponse = checkIfOrganizer(authentication, id);
        if (organizerCheckResponse != null) {
            return organizerCheckResponse;
        }

        try {
            Event event = eventService.updateEvent(id, updatedEvent, authentication.getName());
            return ResponseEntity.ok(event);
        } catch (EventNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "An unexpected error occurred"));
        }
    }

    /**
     * Example of deleteEvent method with role-based access control.
     */
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id, Authentication authentication) {
        String userEmail = authentication.getName();
        try {
            eventService.deleteEvent(id, userEmail);
            return ResponseEntity.noContent().build();
        } catch (EventNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalArgumentException e) {
            // If user is not authorized to delete the event
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}