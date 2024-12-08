package com.example.event_ticketing.services;

import com.example.event_ticketing.models.Event;
import com.example.event_ticketing.models.User;
import com.example.event_ticketing.repositories.EventRepository;
import com.example.event_ticketing.repositories.UserRepository;
import com.example.event_ticketing.exceptions.EventNotFoundException;
import com.example.event_ticketing.exceptions.EventAccessDeniedException;
import com.example.event_ticketing.exceptions.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    // Create a new event
    public Event createEvent(Event event, String userEmail) {
        // Find the organizer (user)
        User organizer = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + userEmail));

        // Set the organizer
        event.setOrganizer(organizer);

        // Save the event
        return eventRepository.save(event);
    }

    // Get all events
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    // Get event by ID
    public Event getEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with ID: " + eventId));
    }

    // Update an event
    public Event updateEvent(Long eventId, Event updatedEvent, String userEmail) {
        Event existingEvent = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with ID: " + eventId));

        // Check if the user is the organizer
        if (!existingEvent.getOrganizer().getEmail().equals(userEmail)) {
            throw new EventAccessDeniedException("You do not have permission to modify this event");
        }

        // Update event details
        existingEvent.setName(updatedEvent.getName());
        existingEvent.setDescription(updatedEvent.getDescription());
        existingEvent.setLocation(updatedEvent.getLocation());
        existingEvent.setDateTime(updatedEvent.getDateTime());

        // Save the updated event
        return eventRepository.save(existingEvent);
    }

    // Delete an event
    public void deleteEvent(Long eventId, String userEmail) {
        Event existingEvent = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with ID: " + eventId));

        // Check if the user is the organizer
        if (!existingEvent.getOrganizer().getEmail().equals(userEmail)) {
            throw new EventAccessDeniedException("You do not have permission to delete this event");
        }

        // Delete the event
        eventRepository.delete(existingEvent);
    }

    // Assign a validator to an event
    @Transactional
    public void assignValidatorToEvent(Long eventId, Long validatorId, String organizerEmail) {
        // Fetch the event
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with ID: " + eventId));

        // Fetch the organizer
        User organizer = userRepository.findByEmail(organizerEmail)
                .orElseThrow(() -> new UserNotFoundException("Organizer not found with email: " + organizerEmail));

        // Validate that the user is the organizer of the event or has ADMIN role
        if (!event.getOrganizer().getId().equals(organizer.getId()) && 
            organizer.getRole() != User.Role.ADMIN) {
            throw new IllegalArgumentException("Access Denied: You are not the organizer or an admin");
        }

        // Fetch the validator
        User validator = userRepository.findById(validatorId)
                .orElseThrow(() -> new UserNotFoundException("Validator not found with ID: " + validatorId));

        // Check if the user has the VALIDATOR role
        if (validator.getRole() != User.Role.VALIDATOR) {
            throw new IllegalArgumentException("User does not have the VALIDATOR role");
        }

        // Assign validator to the event
        if (!event.getValidators().contains(validator)) {
            event.getValidators().add(validator);
            eventRepository.save(event);
        } else {
            throw new IllegalArgumentException("Validator is already assigned to this event");
        }
    }

}
