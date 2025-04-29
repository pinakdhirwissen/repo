package com.example.TicketingSystem.controllers;
import com.example.TicketingSystem.dto.TransferTicketRequest;
import com.example.TicketingSystem.models.TicketComments;
import com.example.TicketingSystem.models.TicketDepartment;
import com.example.TicketingSystem.services.*;
import com.example.TicketingSystem.models.Tickets;
import com.example.TicketingSystem.repositories.TicketRepository;
import com.example.TicketingSystem.repositories.TicketDepartmentRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tickets")
@Tag(name = "Ticket Management", description = "APIs for managing tickets")  // Grouping APIs under "Ticket Management"
public class TicketController {

    @Autowired
    private TicketRepository ticketRepository;
    @Autowired
    private TicketDepartmentRepository ticketDepartmentRepository;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private NotificationService notificationService;
    @Autowired
    private MainEmailService mainEmailService;


    @Operation(summary = "Get Ticket by ID", description = "Fetch a ticket by its unique ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket found"),
            @ApiResponse(responseCode = "404", description = "Ticket not found")
    })
    @GetMapping("/{ticketId}")
    public ResponseEntity<Tickets> getTicketById(
            @Parameter(description = "ID of the ticket to be retrieved") @PathVariable String ticketId) {
        return ticketService.getTicketById(ticketId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/unique")
    public ResponseEntity<List<String>> getUniqueTickets() {
        List<String> tickets = ticketService.getUniqueStatus();
        return tickets.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(tickets);
    }



    @PostMapping
    public ResponseEntity<Tickets> createTicket(@Valid @RequestBody Tickets ticket) {

        Tickets savedTicket = ticketService.createTicket(ticket);
        DateTimeFormatter createdDateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        DateTimeFormatter dueDateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String ticketLink = "http://localhost:5173/ticket/" +savedTicket.getTicketId();

        // Format the createdDate and dueDate
        String formattedCreatedDate = savedTicket.getCreatedDate().format(createdDateFormatter);
        String formattedDueDate = savedTicket.getDueDate().format(dueDateFormatter);
        mainEmailService.sendThymeleafTicketEmail(
                savedTicket.getCreatedBy(),     // Recipient's email
                savedTicket.getTicketId(),     // Ticket ID
                savedTicket.getTitle(),
                savedTicket.getDescription(),
                formattedCreatedDate,
                savedTicket.getPriority(),
                formattedDueDate,
                savedTicket.getStatus(),
                true,   "New Ticket Created"
        );
        notificationService.sendNotification(savedTicket.getCreatedBy(),
                "You Created new request " + savedTicket.getTicketId()
        );

        // ✅ Find active users in the creator's department
        String department = ticket.getDepartment();
        List<TicketDepartment> activeDepartmentUsers = ticketDepartmentRepository.findByDepartment(department)
                .stream()
                .filter(TicketDepartment::getIsActive)

                .toList();

        // ✅ Send email to all active department members
        for (TicketDepartment member : activeDepartmentUsers) {
            if (!member.getEmailId().equals(savedTicket.getCreatedBy())) {

                mainEmailService.sendThymeleafTicketEmail(
                        member.getEmailId(), // Send email to department members
                        savedTicket.getTicketId(),
                        savedTicket.getTitle(),
                        savedTicket.getDescription(),
                        formattedCreatedDate,
                        savedTicket.getPriority(),
                        formattedDueDate,
                        savedTicket.getStatus(),
                        false,
                        "New Ticket in Department"
                );
                notificationService.sendNotification(member.getEmailId(),
                        "A new ticket (ID: " + savedTicket.getTicketId() + ") has been created in your department."
                );
            }
        } // Send notification to department members

        return ResponseEntity.ok(savedTicket);
    }


    @Operation(summary = "Get Tickets by Creator", description = "Retrieve all tickets created by a specific user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tickets retrieved successfully")
    })
    @GetMapping("/createdBy/{email}")
    public ResponseEntity<List<Tickets>> getTicketsByCreatedBy(
            @Parameter(description = "Email of the user who created the tickets") @PathVariable String email) {
        List<Tickets> tickets = ticketService.getTicketsByCreatedBy(email);
        return ResponseEntity.ok(tickets);
    }



    @Operation(summary = "Get Tickets Assigned to User", description = "Retrieve all tickets assigned to a specific user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tickets retrieved successfully")
    })
    @GetMapping("/assignTo/{email}")
    public ResponseEntity<List<Tickets>> getTicketsByAssignTo(
            @Parameter(description = "Email of the user assigned to the tickets") @PathVariable String email) {
        List<Tickets> tickets = ticketService.getTicketsByAssignTo(email);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/search/{email}")
    public ResponseEntity<List<Tickets>> searchTickets(
         @PathVariable String email) {
        List<Tickets> tickets = ticketService.searchTickets(email, email);
        if(tickets.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(tickets);
    }


    @Operation(summary = "Update a Ticket", description = "Update ticket details by its ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket updated successfully"),
            @ApiResponse(responseCode = "404", description = "Ticket not found")
    })



    @PutMapping("/transfer/{ticketId}")
    public ResponseEntity<?> transferTicket(
            @Parameter(description = "ID of the ticket to be transferred") @PathVariable String ticketId,
            @RequestBody TransferTicketRequest transferRequest) {

        Optional<Tickets> optionalTicket = ticketRepository.findByTicketId(ticketId);
        if (optionalTicket.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Tickets ticket = optionalTicket.get();
        ticket.setAssignTo(transferRequest.getEmail());
        ticket.setUpdatedDate(LocalDateTime.now());


        ticketRepository.save(ticket);
        return ResponseEntity.ok(ticket);
    }


    @Operation(summary = "Get Tickets by Department", description = "Retrieve all tickets related to a specific department.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tickets retrieved successfully")
    })
    @GetMapping("/department/{department}")
    public ResponseEntity<List<Tickets>> getTicketsByDepartment(@PathVariable String department) {
        List<Tickets> tickets = ticketRepository.findByDepartment(department);

        if (tickets.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList()); // Return an empty list instead of an error
        }

        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Tickets>> getTicketsByStatus(@PathVariable String status) {
        List<Tickets>tickets = ticketRepository.findByStatus(status);
        if(tickets.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
        }
        return ResponseEntity.ok(tickets);
    }

    @PutMapping("/{ticketId}/assign")
    public ResponseEntity<Tickets> assignTicket(
            @PathVariable String ticketId,
            @RequestParam String assigneeEmail
    ) {
        Optional<Tickets> optionalTicket = ticketRepository.findByTicketId(ticketId);
        if (optionalTicket.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Tickets ticket = optionalTicket.get();
        if (ticket.getAssignTo() != null) {
            return ResponseEntity.badRequest().body(ticket); // Already assigned
        }

        ticket.setAssignTo(assigneeEmail);
        ticket.setStatus("Assigned");
        ticket.setUpdatedDate(LocalDateTime.now());
        ticket.setUpdatedBy(assigneeEmail);
        ticketRepository.save(ticket);

        // ✅ Format dates before passing to email service
        String formattedCreatedDate = ticket.getCreatedDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
        String formattedDueDate = ticket.getDueDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy "));

        // 🔥 Send emails dynamically
        mainEmailService.sendPlainTicketEmail(
                assigneeEmail, ticket.getTicketId(), ticket.getTitle(), ticket.getDescription(),
                formattedCreatedDate, ticket.getPriority(), formattedDueDate,
                "Assigned", "Assigned", assigneeEmail, ticket.getCreatedBy()
        );

        mainEmailService.sendPlainTicketEmail(
                ticket.getCreatedBy(), ticket.getTicketId(), ticket.getTitle(), ticket.getDescription(),
                formattedCreatedDate, ticket.getPriority(), formattedDueDate,
                "Assigned", "Status Changed to Assigned", assigneeEmail, ticket.getCreatedBy()
        );

        return ResponseEntity.ok(ticket);
    }

//     🔥 API to close a ticket
    @PutMapping("/{ticketId}/close")
    public ResponseEntity<Tickets> closeTicket(@PathVariable String ticketId) {
        Optional<Tickets> optionalTicket = ticketRepository.findByTicketId(ticketId);
        if (optionalTicket.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Tickets ticket = optionalTicket.get();
        if (!"Assigned".equals(ticket.getStatus())) {
            return ResponseEntity.badRequest().body(ticket); // Cannot close if not assigned
        }

        ticket.setStatus("Closed");
        ticket.setUpdatedDate(LocalDateTime.now());
        ticketRepository.save(ticket);

        // ✅ Format dates before passing to email service
        String formattedCreatedDate = ticket.getCreatedDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
        String formattedDueDate = ticket.getDueDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy "));
        String feedbackFormUrl = "http://localhost:5173/feedback?ticketId=" + ticketId;

        // 🔥 Send closure emails dynamically
        mainEmailService.sendPlainTicketEmail(
                ticket.getCreatedBy(), ticket.getTicketId(), ticket.getTitle(), ticket.getDescription(),
                formattedCreatedDate, ticket.getPriority(), formattedDueDate,
                "Closed", "Your Ticket is Closed", ticket.getAssignTo(), ticket.getCreatedBy()
        );

        mainEmailService.sendPlainTicketEmail(
                ticket.getAssignTo(), ticket.getTicketId(), ticket.getTitle(), ticket.getDescription(),
                formattedCreatedDate, ticket.getPriority(), formattedDueDate,
                "Closed", "Status Changed to Closed", ticket.getAssignTo(), ticket.getCreatedBy()
        );
        mainEmailService.sendFeedbackRequestEmail(
                ticket.getCreatedBy(), ticket.getTicketId(), ticket.getTitle(),
                "Your ticket has been closed. Please provide feedback for the assignee: " + ticket.getAssignTo() +
                        "\nClick here to submit feedback: " + feedbackFormUrl
        );

        return ResponseEntity.ok(ticket);
    }

    @PutMapping("/{ticketId}/reopen")
    public ResponseEntity<Tickets> reopenTicket(@PathVariable String ticketId) {
        Optional<Tickets> optionalTicket = ticketRepository.findByTicketId(ticketId);
        if (optionalTicket.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Tickets ticket = optionalTicket.get();
        if (!"Closed".equals(ticket.getStatus())) {
            return ResponseEntity.badRequest().body(ticket); // Can only reopen if closed
        }

        ticket.setStatus("Open");
        ticket.setAssignTo(null); // Reset assignee
        ticket.setUpdatedBy(ticket.getCreatedBy());
        ticket.setUpdatedDate(LocalDateTime.now()); // ✅ Update the updatedTime
        ticket.setDueDate(LocalDate.now().plusDays(7));

        ticketRepository.save(ticket);

        // ✅ Notify the creator that the ticket is reopened
        mainEmailService.sendPlainTicketEmail(
                ticket.getCreatedBy(), ticket.getTicketId(), ticket.getTitle(), ticket.getDescription(),
                ticket.getCreatedDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")),
                ticket.getPriority(),
                ticket.getDueDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy ")),
                "Reopened", "Your ticket has been reopened", null, ticket.getCreatedBy()
        );

        notificationService.sendNotification(ticket.getCreatedBy(),"Your Ticket (ID: " + ticket.getTicketId() + ") has been reopened.");

        return ResponseEntity.ok(ticket);
    }

}