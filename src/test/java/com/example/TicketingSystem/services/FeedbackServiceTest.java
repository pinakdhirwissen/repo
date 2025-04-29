package com.example.TicketingSystem.services;

import com.example.TicketingSystem.models.Feedback;
import com.example.TicketingSystem.models.Tickets;
import com.example.TicketingSystem.repositories.FeedbackRepository;
import com.example.TicketingSystem.repositories.TicketRepository;
import com.sun.tools.javac.Main;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FeedbackServiceTest {

    @Mock
    private FeedbackRepository feedbackRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private MainEmailService mainEmailService;



    private FeedbackService feedbackService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        feedbackService = new FeedbackService();
        feedbackService.feedbackRepository = feedbackRepository;
        feedbackService.ticketRepository = ticketRepository;
        feedbackService.mainEmailService = mainEmailService;
    }

    @Test
    void testSubmitFeedback_Success() {
        Tickets ticket = new Tickets();
        ticket.setTicketId("123");

        Feedback feedback = new Feedback();
        feedback.setTicketId(ticket);
        feedback.setAssigneeEmail("assignee@example.com");
        feedback.setRating("5");
        feedback.setComments("Great service!");

        when(ticketRepository.findByTicketId("123")).thenReturn(Optional.of(ticket));
        when(feedbackRepository.save(any(Feedback.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Feedback result = feedbackService.submitFeedback(feedback);

        assertNotNull(result);
        assertEquals(ticket, result.getTicketId());
        verify(ticketRepository, times(1)).findByTicketId("123");
        verify(mainEmailService, times(1)).sendFeedbackToAssignee(
                "assignee@example.com",
                "123",
                "5",
                "Great service!"
        );
        verify(feedbackRepository, times(1)).save(feedback);
    }

    @Test
    void testSubmitFeedback_TicketNotFound() {
        Feedback feedback = new Feedback();
        Tickets ticket = new Tickets();
        ticket.setTicketId("123");
        feedback.setTicketId(ticket);

        when(ticketRepository.findByTicketId("123")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> feedbackService.submitFeedback(feedback));
        assertEquals("Ticket not found", exception.getMessage());
        verify(ticketRepository, times(1)).findByTicketId("123");
        verifyNoInteractions(mainEmailService);
        verifyNoInteractions(feedbackRepository);
    }
}