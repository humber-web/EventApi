package com.example.event_ticketing.config;

import com.example.event_ticketing.security.EventSecurity;
import com.example.event_ticketing.security.TicketSecurity;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {

    private final EventSecurity eventSecurity;
    private final TicketSecurity ticketSecurity;

    public CustomPermissionEvaluator(EventSecurity eventSecurity, TicketSecurity ticketSecurity) {
        this.eventSecurity = eventSecurity;
        this.ticketSecurity = ticketSecurity;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        // Implement if needed
        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if ("Event".equals(targetType)) {
            return eventSecurity.isOrganizer(authentication, (Long) targetId);
        } else if ("Ticket".equals(targetType)) {
            return ticketSecurity.canValidateTicket(authentication, (Long) targetId);
        }
        return false;
    }
}