package com.example.TicketingSystem.controllers;

import com.example.TicketingSystem.models.TicketComments;
import com.example.TicketingSystem.models.Tickets;
import com.example.TicketingSystem.repositories.TicketRepository;
import com.example.TicketingSystem.services.NotificationService;
import com.example.TicketingSystem.services.TicketCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST Controller for handling ticket comments.
 */
@RestController
@RequestMapping("/api/comments")
@Tag(name = "Ticket Comment", description = "APIs for managing commments")
public class TicketCommentsController {

    @Autowired
    private TicketCommentService ticketCommentService;
    @Autowired
    private TicketRepository ticketsRepository;
    @Autowired
    private NotificationService notificationService; // Injec
    /**
     * ✅ 1. Add a new comment to a ticket.
     *
     * @param ticketId   The ID of the ticket to which the comment is being added.
     * @param commentDto The comment details, including the user and message.
     * @return The saved comment object.
     */
    @Operation(summary = "Add a new comment to a ticket", description = "Adds a comment to the specified ticket.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comment added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PostMapping("/{ticketId}/add")
    public ResponseEntity<TicketComments> addComment(
            @Parameter(description = "ID of the ticket to add the comment to") @PathVariable String ticketId,
            @RequestBody TicketComments commentDto
    ) {
        System.out.println("hehehehe");
        Tickets ticket = ticketsRepository.findByTicketId(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found for ID: " + ticketId));
        ticket.setUpdatedBy(commentDto.getCommentedBy());
        ticket.setUpdatedDate(LocalDateTime.now());
        TicketComments savedComment = ticketCommentService.addComment(ticketId, commentDto);
        notificationService.sendNotification(
                commentDto.getCommentedBy(),
                "You added a new comment on ticket " + ticketId
        );
        return ResponseEntity.ok(savedComment);
    }

    /**
     * ✅ 2. Get all comments for a given ticket.
     *
     * @param ticketId The ID of the ticket whose comments are to be retrieved.
     * @return A list of comments associated with the given ticket.
     */
//    @Operation(summary = "Get all comments for a ticket", description = "Fetches all comments associated with the specified ticket ID.")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "Comments retrieved successfully"),
//            @ApiResponse(responseCode = "404", description = "Ticket not found")
//    })
    @GetMapping("/{ticketId}")
    public ResponseEntity<List<TicketComments>> getCommentsByTicketId(
            @Parameter(description = "ID of the ticket whose comments are to be retrieved") @PathVariable Tickets ticketId
    ) {
//        notificationService.sendNotification(
//                ,
//                "You added a n on ticket " + ticketId
//        );
        return ResponseEntity.ok(ticketCommentService.getCommentsByTicket(ticketId));
    }

    /**
     * ✅ 3. Edit a comment (allowed only within 15 minutes).
     *
     * @param commentId      The ID of the comment to be edited.
     * @param comment The object containing the new comment text and user details.
     * @return The updated comment object if successful, or an error message if editing is not allowed.
     */
    @Operation(summary = "Edit an existing comment", description = "Allows editing a comment within 15 minutes of creation.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comment edited successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or editing time exceeded"),
            @ApiResponse(responseCode = "404", description = "Comment not found")
    })
    @PutMapping("/{commentId}/edit")
    public ResponseEntity<?> editComment(
            @Parameter(description = "ID of the comment to be edi" +
                    "ted") @PathVariable String commentId,
            @RequestBody TicketComments comment
    ) {
        try {

            notificationService.sendNotification(comment.getCommentedBy(),
                    "You edited a comment on ticket "
            );

            return ResponseEntity.ok(ticketCommentService.editComment(
                    commentId,
                    comment.getComment(),
                    comment.getCommentedBy()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}