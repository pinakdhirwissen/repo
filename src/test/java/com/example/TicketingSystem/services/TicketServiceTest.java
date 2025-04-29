package com.example.TicketingSystem.services;

import com.example.TicketingSystem.models.TicketDepartment;
import com.example.TicketingSystem.models.Tickets;
import com.example.TicketingSystem.repositories.TicketDepartmentRepository;
import com.example.TicketingSystem.repositories.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TicketServiceTest {

    @Mock private TicketRepository ticketRepository;
    @Mock private AttachmentsService attachmentsService;
    @Mock private CloudinaryService cloudinaryService;
    @Mock private TicketDepartmentRepository ticketDepartmentRepository;
    @Mock private NotificationService notificationService;
    @Mock private MainEmailService mainEmailService;

    @InjectMocks private TicketService ticketService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetTicketById() {
        Tickets ticket = new Tickets();
        when(ticketRepository.findById("1")).thenReturn(Optional.of(ticket));

        Optional<Tickets> result = ticketService.getTicketById("1");
        assertTrue(result.isPresent());
        assertEquals(ticket, result.get());
    }

    @Test
    void testCreateTicket() {
        Tickets ticket = new Tickets();
        when(ticketRepository.save(ticket)).thenReturn(ticket);

        Tickets result = ticketService.createTicket(ticket);
        assertEquals(ticket, result);
    }

    @Test
    void testGetUniqueStatus() {
        Tickets t1 = new Tickets(); t1.setStatus("Open");
        Tickets t2 = new Tickets(); t2.setStatus("Closed");
        when(ticketRepository.findAll()).thenReturn(List.of(t1, t2));

        List<String> statuses = ticketService.getUniqueStatus();
        assertEquals(2, statuses.size());
        assertTrue(statuses.contains("Open"));
        assertTrue(statuses.contains("Closed"));
    }

    @Test
    void testAssignUnassignedTickets() {
        Tickets ticket = new Tickets();
        ticket.setTicketId("1");
        ticket.setStatus("Open");
        ticket.setCreatedDate(LocalDateTime.now().minusMinutes(20));
        ticket.setDepartment("IT");
        ticket.setDueDate(LocalDate.now().plusDays(3));

        TicketDepartment user = mock(TicketDepartment.class);
        when(user.getEmailId()).thenReturn("agent@example.com");
        when(user.getIsActive()).thenReturn(true);

        when(ticketRepository.findByStatusAndCreatedDateBeforeAndAssignToIsNull(eq("Open"), any()))
                .thenReturn(List.of(ticket));
        when(ticketDepartmentRepository.findByDepartment("IT")).thenReturn(List.of(user));
        when(ticketRepository.countByAssignTo("agent@example.com")).thenReturn(0);
        when(ticketRepository.findById("1")).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any())).thenReturn(ticket);

        ticketService.assignUnassignedTickets();

        verify(ticketRepository, times(1)).save(any());
        verify(notificationService).sendNotification(
                eq("agent@example.com"),
                argThat(message -> message != null && message.contains("(ID: 1)"))
        );

        verify(mainEmailService, times(1)).sendThymeleafTicketEmail(any(), any(), any(), any(), any(), any(), any(), any(), eq(false), any());
    }



    @Test
    void testGetTicketsByCreatedBy() {
        List<Tickets> tickets = List.of(new Tickets());
        when(ticketRepository.findByCreatedBy("user")).thenReturn(tickets);
        assertEquals(tickets, ticketService.getTicketsByCreatedBy("user"));
    }

    @Test
    void testGetTicketsByAssignTo() {
        List<Tickets> tickets = List.of(new Tickets());
        when(ticketRepository.findByAssignTo("user")).thenReturn(tickets);
        assertEquals(tickets, ticketService.getTicketsByAssignTo("user"));
    }

    @Test
    void testUpdateTicket() {
        Tickets original = new Tickets();
        original.setTicketId("1");

        Tickets update = new Tickets();
        update.setTitle("Updated");
        update.setDescription("Updated desc");
        update.setDepartment("HR");
        update.setAssignTo("newUser");
        update.setPriority("High");
        update.setDueDate(LocalDate.now());
        update.setUpdatedBy("admin");

        when(ticketRepository.findById("1")).thenReturn(Optional.of(original));
        when(ticketRepository.save(any())).thenReturn(original);

        Optional<Tickets> result = ticketService.updateTicket("1", update);
        assertTrue(result.isPresent());
        assertEquals("Updated", result.get().getTitle());
    }

    @Test
    void testFindByTicketId_Success() {
        Tickets ticket = new Tickets();
        when(ticketRepository.findByTicketId("1")).thenReturn(Optional.of(ticket));
        assertEquals(ticket, ticketService.findByTicketId("1"));
    }

    @Test
    void testFindByTicketId_NotFound() {
        when(ticketRepository.findByTicketId("1")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> ticketService.findByTicketId("1"));
    }

    @Test
    void testSearchTickets() {
        List<Tickets> tickets = List.of(new Tickets());

        when(ticketRepository.findByCreatedByOrAssignTo(eq("a"), eq("b"))).thenReturn(tickets);

        assertEquals(tickets, ticketService.searchTickets("a", "b"));
    }


    @Test
    void testAssignTicket() {
        Tickets ticket = new Tickets();
        ticket.setTicketId("1");

        when(ticketRepository.findById("1")).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(ticket)).thenReturn(ticket);

        Tickets result = ticketService.assignTicket("1", "user@example.com");

        assertEquals("user@example.com", result.getAssignTo());
        assertEquals("Assigned", result.getStatus());
    }

    @Test
    void testSendRemindersToAssignees_ShortDue() {
        Tickets ticket = new Tickets();
        ticket.setTicketId("1");
        ticket.setAssignTo("agent@example.com");
        ticket.setStatus("Open");
        ticket.setDueDate(LocalDate.now().plusDays(1));
        ticket.setCreatedDate(LocalDateTime.now().minusDays(1));
        ticket.setUpdatedDate(LocalDateTime.now());

        when(ticketRepository.findAll()).thenReturn(List.of(ticket));

        ticketService.sendRemindersToAssignees();

        verify(mainEmailService).sendThymeleafTicketEmail(any(),eq("1"),any(), any(), any(), any(), any(), any(),  eq(false), any());
        verify(notificationService).sendNotification(eq("agent@example.com"), contains("Ticket 1"));
    }

    @Test
    void testSendRemindersToAssignees_Overdue() {
        Tickets ticket = new Tickets();
        ticket.setTicketId("2");
        ticket.setDescription("Testing overdue logic");
        ticket.setCreatedBy("agent@example.com");
        ticket.setAssignTo("agent@example.com");
        ticket.setStatus("In Progress");
        ticket.setPriority("High");

        ticket.setDueDate(LocalDate.now().minusDays(1));
        ticket.setCreatedDate(LocalDateTime.now().minusDays(5));
        ticket.setUpdatedDate(LocalDateTime.now());

        when(ticketRepository.findAll()).thenReturn(List.of(ticket));

        ticketService.sendRemindersToAssignees();


        verify(mainEmailService).sendThymeleafTicketEmail(
                eq("agent@example.com"),
                eq("2"),
                any(),
                any(),
                any(),
                any(),
                any(),
                eq("In Progress"),
                eq(false),
                eq("⏳ Reminder: Ticket ID: 2 is overdue!")
        );

        verify(notificationService).sendNotification(eq("agent@example.com"), contains("Ticket 2"));
    }

}
