package com.example.TicketingSystem.controllers;
import com.example.TicketingSystem.models.Feedback;
import com.example.TicketingSystem.models.Tickets;
import com.example.TicketingSystem.repositories.FeedbackRepository;
import com.example.TicketingSystem.repositories.TicketRepository;
import com.example.TicketingSystem.services.FeedbackService;
import com.example.TicketingSystem.services.NotificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

@Tag(name = "Feedback API", description = "Endpoints for managing feedback in the ticketing system")
@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;
    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private FeedbackRepository feedbackRepository;
    @Autowired
    private NotificationService notificationService;

    //    @PostMapping("/submit")
//    @Transactional
//    public ResponseEntity<Feedback> submitFeedback(@RequestBody Feedback feedback) {
//        Feedback savedFeedback = feedbackService.submitFeedback(feedback);
//        return ResponseEntity.ok(savedFeedback);
//    }
    @PostMapping("/submit/{ticketId}")
    @Transactional
    public ResponseEntity<Feedback> submitFeedback(
            @PathVariable String ticketId,
            @RequestBody Feedback feedback) {
        System.out.println(feedback);

        // Fetch the Ticket entity by ticketId from the database
        Tickets ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));

        // Now associate the Ticket with the Feedback
        feedback.setTicketId(ticket);

        // Save the Feedback entity
        Feedback savedFeedback = feedbackService.submitFeedback(feedback);
        System.out.println(savedFeedback);
        notificationService.sendNotification(ticket.getCreatedBy(),
                "New feedback has been added to your ticket (ID: " + ticket.getTicketId() + ").");

        return ResponseEntity.ok(savedFeedback);
    }
}