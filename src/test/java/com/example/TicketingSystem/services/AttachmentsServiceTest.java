package com.example.TicketingSystem.services;

import com.example.TicketingSystem.models.Attachments;
import com.example.TicketingSystem.models.Tickets;
import com.example.TicketingSystem.repositories.AttachmentsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class AttachmentsServiceTest {

    @Mock
    private AttachmentsRepository attachmentsRepository;

    private AttachmentsService attachmentsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        attachmentsService = new AttachmentsService();
        attachmentsService.attachmentsRepository = attachmentsRepository;
    }

    @Test
    void testSaveAttachment() {
        Attachments attachment = new Attachments();
        when(attachmentsRepository.save(attachment)).thenReturn(attachment);

        Attachments result = attachmentsService.saveAttachment(attachment);

        assertEquals(attachment, result);
        verify(attachmentsRepository, times(1)).save(attachment);
    }

    @Test
    void testGetAttachmentsByTicketId() {
        Tickets ticket = new Tickets();
        ticket.setTicketId("123");
        Attachments attachment = new Attachments();
        when(attachmentsRepository.findByTicketId("123")).thenReturn(List.of(attachment));

        List<Attachments> result = attachmentsService.getAttachmentsByTicketId(ticket);

        assertEquals(1, result.size());
        assertEquals(attachment, result.get(0));
        verify(attachmentsRepository, times(1)).findByTicketId("123");
    }

    @Test
    void testGetAttachmentsByTicketId_NoAttachments() {
        Tickets ticket = new Tickets();
        ticket.setTicketId("123");
        when(attachmentsRepository.findByTicketId("123")).thenReturn(Collections.emptyList());

        List<Attachments> result = attachmentsService.getAttachmentsByTicketId(ticket);


        assertTrue(result.isEmpty());
        verify(attachmentsRepository, times(1)).findByTicketId("123");
    }
}