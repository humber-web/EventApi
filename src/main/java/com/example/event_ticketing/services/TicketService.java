package com.example.event_ticketing.services;

import com.example.event_ticketing.models.Ticket;
import com.example.event_ticketing.models.User;
import com.example.event_ticketing.models.Event; // Ensure this import is present
import com.example.event_ticketing.models.Ticket.TicketStatus;
import com.example.event_ticketing.repositories.TicketRepository;
import com.example.event_ticketing.repositories.EventRepository;
import com.example.event_ticketing.repositories.UserRepository;
import com.example.event_ticketing.utils.QRCodeUtil;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import com.example.event_ticketing.exceptions.TicketNotFoundException;
import com.example.event_ticketing.dto.TicketCreationRequest;
import com.example.event_ticketing.dto.TicketPurchaseDetail;
import com.example.event_ticketing.dto.TicketPurchaseRequest;
import com.example.event_ticketing.exceptions.TicketAlreadySoldException;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import com.example.event_ticketing.exceptions.TicketAlreadyValidatedException;
import com.example.event_ticketing.exceptions.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.event_ticketing.exceptions.EventNotFoundException;
import com.example.event_ticketing.exceptions.InvalidTicketStatusException;
import com.example.event_ticketing.exceptions.NotEnoughTicketsException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.mail.javamail.JavaMailSender;

@Service
public class TicketService {

     @Value("${qr.codes.dir}")  // Inject the property
    private String qrCodesDir;
    // private static final String qrCodesDir = "/home/humber/Documents/Projects/EventApi/event-ticketing/src/main/java/com/example/event_ticketing/images"; // Define
                                                                                                                                                            // the
                                                                                                                                                            // qrCodesDir
                                                                                                                                                            // variable

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventService eventService;

    @Autowired
    private QRCodeUtil qrCodeUtil;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private JavaMailSender mailSender;

    private void createQrCodeImage(String qrCodeData, String filePath) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(qrCodeData, BarcodeFormat.QR_CODE, 200, 200);

        Path path = FileSystems.getDefault().getPath(filePath);
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);
    }

    @Transactional
    public Map<String, Object> purchaseTickets(TicketPurchaseRequest purchaseRequest, String userEmail) {
        System.out.println("Starting purchaseTickets with Event ID: " + purchaseRequest.getEventId()
                + " and User Email: " + userEmail);

        Long eventId = purchaseRequest.getEventId();
        List<TicketPurchaseDetail> purchases = purchaseRequest.getPurchases();

        // 1. Validate event
        Event event = eventService.getEventById(eventId);
        if (event == null) {
            System.out.println("Event not found with ID: " + eventId);
            throw new EventNotFoundException("Event not found with ID: " + eventId);
        }
        System.out.println("Event validated: " + event);

        // 2. Fetch buyer
        User buyer = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    System.out.println("User not found with email: " + userEmail);
                    return new UserNotFoundException("User not found with email: " + userEmail);
                });
        System.out.println("Buyer found: " + buyer);

        List<Ticket> purchasedTickets = new ArrayList<>();
        List<String> qrCodePaths = new ArrayList<>();
        double totalAmount = 0.0;

        // 3. Process each purchase detail
        for (TicketPurchaseDetail detail : purchases) {
            String ticketType = detail.getType();
            int quantity = detail.getQuantity();
            System.out.println("Processing purchase detail: Type = " + ticketType + ", Quantity = " + quantity);

            // Fetch available tickets for this type
            List<Ticket> availableTickets = ticketRepository.findByEventIdAndTypeAndStatus(
                    eventId, ticketType, Ticket.TicketStatus.AVAILABLE);

            System.out.println("Available tickets for type " + ticketType + ": " + availableTickets.size());

            if (availableTickets.size() < quantity) {
                System.out.println("Not enough tickets available for type " + ticketType + ". Requested: " + quantity
                        + ", Available: " + availableTickets.size());
                throw new IllegalArgumentException("Not enough tickets available for type " + ticketType +
                        ". Requested: " + quantity + ", Available: " + availableTickets.size());
            }

            List<Ticket> ticketsToPurchase = availableTickets.subList(0, quantity);

            for (Ticket ticket : ticketsToPurchase) {
                ticket.setStatus(Ticket.TicketStatus.SOLD);
                ticket.setBuyer(buyer);
                String qrData = "TICKET_ID:" + ticket.getId();
                ticket.setQrCodeData(qrData);
                totalAmount += ticket.getPrice();
                purchasedTickets.add(ticket);
                System.out.println("Purchased Ticket ID: " + ticket.getId() + ", QR Data: " + qrData);
            }

            // Save the updated tickets
            ticketRepository.saveAll(ticketsToPurchase);
            System.out.println("Saved " + ticketsToPurchase.size() + " tickets for type " + ticketType);
        }

        // 4. Send QR codes via single email
        for (Ticket ticket : purchasedTickets) {
            String qrData = ticket.getQrCodeData();
            String filePath = qrCodesDir + "/QR_TICKET_" + ticket.getId() + ".png";
            try {
                createQrCodeImage(qrData, filePath);
                qrCodePaths.add(filePath);
                System.out.println("Generated QR code for Ticket ID: " + ticket.getId());
            } catch (WriterException | IOException e) {
                System.out.println("Failed to generate QR code for Ticket ID: " + ticket.getId());
                e.printStackTrace();
                throw new RuntimeException("Failed to generate QR code for Ticket ID: " + ticket.getId(), e);
            }
        }

        try {
            sendQrCodeEmail(buyer.getEmail(), qrCodePaths);
            System.out.println("Sent consolidated QR code email to: " + buyer.getEmail());
        } catch (MessagingException e) {
            System.out.println("Failed to send QR code email to: " + buyer.getEmail());
            e.printStackTrace();
            throw new RuntimeException("Failed to send QR code email to: " + buyer.getEmail(), e);
        }

        // 5. Prepare purchase response
        Map<String, Object> response = new HashMap<>();
        response.put("purchasedTickets", purchasedTickets);
        response.put("totalAmount", totalAmount);
        System.out.println("Total Amount for Purchase: " + totalAmount);
        System.out.println("Purchase completed successfully with " + purchasedTickets.size() + " tickets.");
        return response;
    }
    // Method to send QR code email
    @Async
    private void sendQrCodeEmail(String toEmail, List<String> qrCodePaths) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(toEmail);
        helper.setSubject("Your Tickets and QR Codes");
        helper.setText(
                "Dear User,\n\nThank you for your purchase. Please find your tickets' QR codes attached.\n\nBest regards,\nEvent Team");

        for (String qrCodePath : qrCodePaths) {
            File qrFile = new File(qrCodePath);
            helper.addAttachment("QR_TICKET_" + qrFile.getName(), qrFile);
        }

        mailSender.send(message);
    }


    // Get all tickets (optional)
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    // Get tickets purchased by a user
    public List<Ticket> getTicketsByBuyerEmail(String userEmail) {
        User buyer = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + userEmail));

        return ticketRepository.findByBuyerId(buyer.getId());
    }

    // Create multiple tickets (Organizer)
    public List<Ticket> createTickets(TicketCreationRequest ticketRequest) throws WriterException, IOException {
        Event event = eventService.getEventById(ticketRequest.getEventId());

        // Create directory if it doesn't exist
        Path qrDirPath = FileSystems.getDefault().getPath(qrCodesDir);
        if (!Files.exists(qrDirPath)) {
            Files.createDirectories(qrDirPath);
        }

        List<Ticket> tickets = new ArrayList<>();
        for (int i = 0; i < ticketRequest.getQuantity(); i++) {
            Ticket ticket = new Ticket();
            ticket.setType(ticketRequest.getType());
            ticket.setPrice(ticketRequest.getPrice());
            ticket.setEvent(event);
            ticket.setStatus(Ticket.TicketStatus.AVAILABLE);
            String qrData = UUID.randomUUID().toString();
            ticket.setQrCodeData(qrData);
            System.out.println("Generated QR Code Data: " + qrData);

            // Generate and save QR code image
            String filePath = qrCodesDir + "/QR_" + qrData + ".png";
            createQrCodeImage(qrData, filePath);

            tickets.add(ticket);
        }

        return ticketRepository.saveAll(tickets);
    }

    // Purchase multiple tickets
    /**
     * Handles the purchase of multiple tickets.
     *
     * @param eventId   ID of the event.
     * @param type      Type of the tickets.
     * @param quantity  Number of tickets to purchase.
     * @param userEmail Email of the buyer.
     * @return A map containing purchased tickets and the total amount.
     */
    @Transactional
    public Ticket purchaseTicket(Long ticketId, String userEmail) {
        System.out.println("Starting purchaseTicket with ticketId: " + ticketId + " and userEmail: " + userEmail);

        // Find the ticket by ID
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> {
                    System.out.println("Ticket not found with ID: " + ticketId);
                    return new TicketNotFoundException("Ticket not found with ID: " + ticketId);
                });

        System.out.println("Found ticket: " + ticket);

        // Check if the ticket is available
        if (ticket.getStatus() == TicketStatus.SOLD) {
            System.out.println("Ticket with ID " + ticketId + " is already sold.");
            throw new TicketAlreadySoldException("Ticket with ID " + ticketId + " has already been sold");
        } else {
            System.out.println("Ticket with ID " + ticketId + " is available for purchase.");
        }

        // Find the buyer by email
        User buyer = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    System.out.println("User not found with email: " + userEmail);
                    return new UserNotFoundException("User not found with email: " + userEmail);
                });

        System.out.println("Buyer found: " + buyer);

        // Simulate payment processing here (assumed successful)
        System.out.println("Simulating payment processing for user: " + userEmail);
        // [Payment processing logic can be added here]
        System.out.println("Payment processed successfully.");

        // Update ticket status and assign buyer
        ticket.setStatus(TicketStatus.SOLD);
        ticket.setBuyer(buyer);
        System.out.println("Ticket status updated to SOLD and buyer assigned.");

        // Generate QR code data using ticket ID
        String qrData = "TICKET_ID:" + ticket.getId();
        ticket.setQrCodeData(qrData);
        System.out.println("QR code data set: " + qrData);

        // Generate and save QR code image
        String filePath = qrCodesDir + "/QR_TICKET_" + ticket.getId() + ".png";
        System.out.println("QR code file path: " + filePath);
        try {
            // Create directory if it doesn't exist
            Path qrDirPath = FileSystems.getDefault().getPath(qrCodesDir);
            if (!Files.exists(qrDirPath)) {
                Files.createDirectories(qrDirPath);
                System.out.println("qrCodesDir created at: " + qrCodesDir);
            } else {
                System.out.println("qrCodesDir already exists.");
            }

            createQrCodeImage(qrData, filePath);
            System.out.println("QR code image created at: " + filePath);
        } catch (WriterException | IOException e) {
            System.out.println("Failed to generate QR code for ticket ID: " + ticket.getId());
            e.printStackTrace();
            throw new RuntimeException("Failed to generate QR code for ticket ID: " + ticket.getId(), e);
        }

        // Send QR code via email
        // try {
        //     sendQrCodeEmail(buyer.getEmail(), filePath);
        //     System.out.println("QR code email sent to: " + buyer.getEmail());
        // } catch (MessagingException e) {
        //     System.out.println("Failed to send QR code email to: " + buyer.getEmail());
        //     e.printStackTrace();
        //     throw new RuntimeException("Failed to send QR code email to: " + buyer.getEmail(), e);
        // }

        // Save the updated ticket
        Ticket savedTicket = ticketRepository.save(ticket);
        System.out.println("Ticket saved successfully: " + savedTicket);

        System.out.println("Ticket purchased successfully: Ticket ID " + ticketId + ", Buyer Email " + userEmail);
        return savedTicket;
    }

    // Get a ticket by ID
    public Ticket getTicketById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found with ID: " + id));
    }

    public List<Ticket> getTicketsByEventId(Long eventId) {
        Event event = eventService.getEventById(eventId);
        if (event == null) {
            throw new EventNotFoundException("Event not found with ID: " + eventId);
        }
        return ticketRepository.findByEventId(eventId);
    }

    // Validate a ticket
    @Transactional
    public Ticket validateTicket(Long ticketId, Long validatorId) {
        // Fetch the ticket
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found with ID: " + ticketId));

        // Check if the ticket is in SOLD status
        if (ticket.getStatus() != TicketStatus.SOLD) {
            throw new InvalidTicketStatusException(
                    "Ticket with ID " + ticketId + " cannot be validated because its status is " + ticket.getStatus());
        }

        // Check if the ticket is already validated
        if (ticket.getStatus() == TicketStatus.VALIDATED) {
            throw new TicketAlreadyValidatedException("Ticket with ID " + ticketId + " has already been validated");
        }

        // Fetch the validator (user)
        User validator = userRepository.findById(validatorId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + validatorId));

        // Verify that the user has the VALIDATOR or ADMIN role
        if (validator.getRole() != User.Role.VALIDATOR && validator.getRole() != User.Role.ADMIN) {
            throw new IllegalArgumentException(
                    "User with ID " + validatorId + " does not have permission to validate tickets");
        }

        // Update the ticket status and assign the validator
        ticket.setStatus(TicketStatus.VALIDATED);
        ticket.setValidator(validator);

        // Save the updated ticket
        return ticketRepository.save(ticket);
    }

    // Update a ticket
    public Ticket updateTicket(Long id, Ticket updatedTicket) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found with ID: " + id));

        // Update ticket details
        ticket.setEvent(updatedTicket.getEvent());
        ticket.setPrice(updatedTicket.getPrice());
        ticket.setStatus(updatedTicket.getStatus());
        ticket.setBuyer(updatedTicket.getBuyer());

        return ticketRepository.save(ticket);
    }

    // Delete a ticket
    public void deleteTicket(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found with ID: " + id));

        ticketRepository.delete(ticket);
    }
}