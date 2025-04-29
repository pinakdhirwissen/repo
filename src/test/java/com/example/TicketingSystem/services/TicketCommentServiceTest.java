package com.example.TicketingSystem.services;

import com.example.TicketingSystem.models.TicketComments;
import com.example.TicketingSystem.models.Tickets;
import com.example.TicketingSystem.repositories.TicketCommentsRepository;
import com.example.TicketingSystem.repositories.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TicketCommentServiceTest {

    @Mock
    private TicketCommentsRepository ticketCommentRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private MainEmailService mainEmailService;

    @InjectMocks
    private TicketCommentService ticketCommentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetCommentsByTicket() {
        Tickets ticket = new Tickets();
        TicketComments comment1 = new TicketComments();
        TicketComments comment2 = new TicketComments();
        when(ticketCommentRepository.findByTicketId(ticket)).thenReturn(List.of(comment1, comment2));

        List<TicketComments> result = ticketCommentService.getCommentsByTicket(ticket);

        assertEquals(2, result.size());
        verify(ticketCommentRepository, times(1)).findByTicketId(ticket);
    }

    @Test
    public void testGetCommentsByTicket_EmptyList() {
        Tickets ticket = new Tickets();
        when(ticketCommentRepository.findByTicketId(ticket)).thenReturn(List.of());

        List<TicketComments> result = ticketCommentService.getCommentsByTicket(ticket);

        assertTrue(result.isEmpty());
        verify(ticketCommentRepository, times(1)).findByTicketId(ticket);
    }

    @Test
    public void testAddComment_Success() {
        String ticketId = "123";
        Tickets ticket = new Tickets();
        ticket.setTicketId(ticketId);
        ticket.setCreatedBy("user@example.com");

        TicketComments commentDto = new TicketComments();
        commentDto.setComment("Test comment");
        commentDto.setCommentedBy("user@example.com");

        when(ticketRepository.findByTicketId(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketCommentRepository.save(any(TicketComments.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TicketComments result = ticketCommentService.addComment(ticketId, commentDto);

        assertNotNull(result);
        assertEquals("Test comment", result.getComment());
        assertEquals("user@example.com", result.getCommentedBy());
        assertNotNull(result.getCommentedDate());
        assertEquals(ticket, result.getTicketId());

        verify(mainEmailService, times(1)).sendCommentEmail(
                eq("user@example.com"),
                eq(ticketId),
                any(),
                eq("Test comment"),
                eq("user@example.com"),
                eq(false)
        );
    }


        @Test
    public void testAddComment_NullFieldsInCommentDto() {
        String ticketId = "123";
        Tickets ticket = new Tickets();
        ticket.setTicketId(ticketId);
        ticket.setCreatedBy("user@example.com");

        TicketComments commentDto = new TicketComments(); // comment and commentedBy are null

        when(ticketRepository.findByTicketId(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketCommentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        TicketComments result = ticketCommentService.addComment(ticketId, commentDto);

        assertNull(result.getComment());
        assertNull(result.getCommentedBy());
        assertNotNull(result.getCommentedDate());
    }

    @Test
    public void testEditComment_AlreadyEditedComment() {
        String commentId = "1";
        String newComment = "Updated comment";
        String user = "user@example.com";
        Tickets ticket = new Tickets();
        ticket.setTicketId("T-001");
        ticket.setCreatedBy("creator@example.com");

        TicketComments existingComment = new TicketComments();
        existingComment.setCommentId(1L);
        existingComment.setCommentedBy(user);
        existingComment.setCommentedDate(LocalDateTime.now().minusMinutes(5));
        existingComment.setEdited(true);
        existingComment.setTicketId(ticket);

        when(ticketCommentRepository.findByCommentId(Long.parseLong(commentId))).thenReturn(Optional.of(existingComment));
        when(ticketCommentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        TicketComments result = ticketCommentService.editComment(commentId, newComment, user);

        assertTrue(result.isEdited());
        assertNotNull(result.getEditedDate());
    }



    @Test
    public void testAddComment_TicketNotFound() {
        String ticketId = "123";
        TicketComments commentDto = new TicketComments();
        when(ticketRepository.findByTicketId(ticketId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> ticketCommentService.addComment(ticketId, commentDto));
    }

    @Test
    public void testEditComment_Success() {
        String commentId = "1";
        String newComment = "Updated comment";
        String user = "user@example.com";
        Tickets ticket = new Tickets();
        ticket.setTicketId("T-001");
        ticket.setCreatedBy("creator@example.com");

        TicketComments existingComment = new TicketComments();
        existingComment.setCommentId(1L);
        existingComment.setCommentedBy(user);
        existingComment.setCommentedDate(LocalDateTime.now().minusMinutes(10));
        existingComment.setTicketId(ticket);

        when(ticketCommentRepository.findByCommentId(Long.parseLong(commentId))).thenReturn(Optional.of(existingComment));
        when(ticketCommentRepository.save(any(TicketComments.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TicketComments result = ticketCommentService.editComment(commentId, newComment, user);

        assertNotNull(result);
        assertEquals(newComment, result.getComment());
        assertTrue(result.isEdited());
        assertNotNull(result.getEditedDate());
        assertTrue(result.getEditedDate().isAfter(result.getCommentedDate()));

        verify(mainEmailService, times(1)).sendCommentEmail(
                eq("creator@example.com"), eq("T-001"), anyLong(), eq(newComment), eq(user), eq(true));
    }

    @Test
    public void testEditComment_CommentNotFound() {
        String commentId = "1";
        String newComment = "Updated comment";
        String user = "user@example.com";

        when(ticketCommentRepository.findByCommentId(Long.parseLong(commentId))).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> ticketCommentService.editComment(commentId, newComment, user));
    }

    @Test
    public void testEditComment_UnauthorizedUser() {
        String commentId = "1";
        String newComment = "Updated comment";
        String user = "unauthorized@example.com";
        TicketComments existingComment = new TicketComments();
        existingComment.setCommentId(1L);
        existingComment.setCommentedBy("authorized@example.com");

        when(ticketCommentRepository.findByCommentId(Long.parseLong(commentId))).thenReturn(Optional.of(existingComment));

        assertThrows(IllegalArgumentException.class, () -> ticketCommentService.editComment(commentId, newComment, user));
    }

    @Test
    public void testEditComment_ExpiredEditTime() {
        String commentId = "1";
        String newComment = "Updated comment";
        String user = "user@example.com";
        TicketComments existingComment = new TicketComments();
        existingComment.setCommentId(1L);
        existingComment.setCommentedBy(user);
        existingComment.setCommentedDate(LocalDateTime.now().minusMinutes(20));

        when(ticketCommentRepository.findByCommentId(Long.parseLong(commentId))).thenReturn(Optional.of(existingComment));

        assertThrows(IllegalArgumentException.class, () -> ticketCommentService.editComment(commentId, newComment, user));
    }

    @Test
    public void testEditComment_RepositoryException() {
        String commentId = "1";
        String newComment = "Updated comment";
        String user = "user@example.com";
        TicketComments existingComment = new TicketComments();
        existingComment.setCommentId(1L);
        existingComment.setCommentedBy(user);

        when(ticketCommentRepository.findByCommentId(Long.parseLong(commentId))).thenReturn(Optional.of(existingComment));
        when(ticketCommentRepository.save(any(TicketComments.class))).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> ticketCommentService.editComment(commentId, newComment, user));
    }

    @Test
    public void testEditComment_NullComment() {
        String commentId = "1";
        String user = "user@example.com";
        assertThrows(IllegalArgumentException.class, () -> ticketCommentService.editComment(commentId, null, user));
    }

    @Test
    public void testAddComment_RepositoryException() {
        String ticketId = "123";
        Tickets ticket = new Tickets();
        ticket.setTicketId(ticketId);
        TicketComments commentDto = new TicketComments();
        commentDto.setComment("Test comment");

        when(ticketRepository.findByTicketId(ticketId)).thenReturn(Optional.of(ticket));
        when(ticketCommentRepository.save(any(TicketComments.class))).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> ticketCommentService.addComment(ticketId, commentDto));
    }

    @Test
    public void testAddComment_NullComment() {
        String ticketId = "123";
        assertThrows(IllegalArgumentException.class, () -> ticketCommentService.addComment(ticketId, null));
    }
    @Test
    public void testGetCommentsByTicket_NullTicket() {
        assertThrows(IllegalArgumentException.class, () -> ticketCommentService.getCommentsByTicket(null));
    }
}
