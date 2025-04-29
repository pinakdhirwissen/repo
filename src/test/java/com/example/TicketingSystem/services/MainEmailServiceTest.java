package com.example.TicketingSystem.services;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MainEmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @InjectMocks
    private MainEmailService mainEmailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSendThymeleafTicketEmail_Success() throws Exception {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        when(templateEngine.process(eq("ticket-email"), any(Context.class))).thenReturn("<html>Email Content</html>");

        mainEmailService.sendThymeleafTicketEmail(
                "test@example.com",
                "123",
                "Test Title",
                "Test Description",
                "2023-10-01",
                "High",
                "2023-10-10",
                "Open",
                true,
                "Test Header"
        );

        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
        verify(templateEngine, times(1)).process(eq("ticket-email"), any(Context.class));
    }

    @Test
    void testSendPlainTicketEmail_Success() throws Exception {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        mainEmailService.sendPlainTicketEmail(
                "test@example.com",
                "123",
                "Test Title",
                "Test Description",
                "2023-10-01",
                "High",
                "2023-10-10",
                "Open",
                "Test Subject",
                "Assignee",
                "Creator"
        );

        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void testSendCommentEmail_Success() {
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        mainEmailService.sendCommentEmail(
                "test@example.com",
                "123",
                1L,
                "Test Comment",
                "Commenter",
                false
        );

        verify(mailSender, times(1)).send(captor.capture());
        SimpleMailMessage sentMessage = captor.getValue();

        assertEquals("test@example.com", sentMessage.getTo()[0]);
        assertEquals("New Comment on Ticket #123", sentMessage.getSubject());
        assertTrue(sentMessage.getText().contains("Test Comment"));
    }

    @Test
    void testSendFeedbackRequestEmail_Success() throws Exception {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        mainEmailService.sendFeedbackRequestEmail(
                "test@example.com",
                "123",
                "Test Title",
                "Please provide feedback"
        );

        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void testSendFeedbackToAssignee_Success() throws Exception {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        mainEmailService.sendFeedbackToAssignee(
                "assignee@example.com",
                "123",
                "5",
                "Great service!"
        );

        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
    }
}