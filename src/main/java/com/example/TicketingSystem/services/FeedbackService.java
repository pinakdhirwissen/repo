package com.example.TicketingSystem.services;

import com.example.TicketingSystem.models.Feedback;
import com.example.TicketingSystem.models.Tickets;
import com.example.TicketingSystem.repositories.FeedbackRepository;
import com.example.TicketingSystem.repositories.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FeedbackService {

    @Autowired
    public FeedbackRepository feedbackRepository;


    @Autowired
    public TicketRepository ticketRepository;

    @Autowired
    public MainEmailService mainEmailService;

    public Feedback submitFeedback(Feedback feedback) {
        Tickets ticket = ticketRepository.findByTicketId(feedback.getTicketId().getTicketId())
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));

        feedback.setTicketId(ticket);
        mainEmailService.sendFeedbackToAssignee(
                feedback.getAssigneeEmail(),
                feedback.getTicketId().getTicketId(),
                feedback.getRating(),
                feedback.getComments()
        );

        return feedbackRepository.save(feedback);
    }
}