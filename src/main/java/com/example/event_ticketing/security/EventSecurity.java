package com.example.event_ticketing.security;

import com.example.event_ticketing.models.Event;
import com.example.event_ticketing.services.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("eventSecurity")
public class EventSecurity {

    @Autowired
    private EventService eventService;

    /**
     * Checks if the authenticated user is the organizer of the specified event.
     *
     * @param authentication The authentication object containing user details.
     * @param eventId        The ID of the event.
     * @return true if the user is the organizer, false otherwise.
     */
    public boolean isOrganizer(Authentication authentication, Long eventId) {
        Event event = eventService.getEventById(eventId);
        String userEmail = authentication.getName();
        boolean isOrganizer = event.getOrganizer().getEmail().equals(userEmail);
        
        // Log the result of the check
        System.out.println("User Email: " + userEmail);
        System.out.println("Organizer Email: " + event.getOrganizer().getEmail());
        System.out.println("Is Organizer: " + isOrganizer);
        
        return isOrganizer;
    }
    
}
