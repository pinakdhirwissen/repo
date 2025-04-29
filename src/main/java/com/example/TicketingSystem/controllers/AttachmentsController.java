package com.example.TicketingSystem.controllers;
import com.example.TicketingSystem.models.Attachments;
import com.example.TicketingSystem.models.Tickets;
import com.example.TicketingSystem.services.AttachmentsService;
import com.example.TicketingSystem.services.CloudinaryService;
import com.example.TicketingSystem.services.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

/**
 * REST Controller for handling file attachments related to tickets.
 */
@Tag(name = "Attachments API", description = "Endpoints for managing attachments in the ticketing system")
@RestController
@RequestMapping("/api/attachments")
public class AttachmentsController {

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private AttachmentsService attachmentsService; // ✅ Use service instead of repository

    @Autowired
    private TicketService ticketService;

    /**
     * Uploads a file and associates it with a ticket.
     *
     * @param file The file to be uploaded.
     * @param ticketId The ID of the ticket to which the file belongs.
     * @return The saved attachment object or an error message if the upload fails.
     */
    @Operation(summary = "Upload a file for a ticket",
            description = "Uploads a file and links it to the specified ticket.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "File upload failed")
    })
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file,
                                        @RequestParam("ticketId") String ticketId) {
        try {
            Tickets ticket = ticketService.findByTicketId(ticketId);
            String fileUrl = cloudinaryService.uploadFile(file);

            Attachments attachment = new Attachments();
            attachment.setTicketId(ticket);
            attachment.setFilePath(fileUrl);
            attachment.setTypeOfFile(file.getContentType());
            attachment.setFileName(file.getOriginalFilename());

            Attachments savedAttachment = attachmentsService.saveAttachment(attachment); // ✅ Call service method

            return ResponseEntity.ok(savedAttachment);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("File upload failed: " + e.getMessage());
        }
    }

    /**
     * Retrieves all attachments associated with a given ticket ID.
     *
     * @param ticketId The ID of the ticket whose attachments need to be retrieved.
     * @return A list of attachments for the specified ticket.
     */
    @Operation(summary = "Get attachments by ticket ID",
            description = "Fetches all attachments related to a specific ticket.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attachments retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Ticket not found")
    })
    @GetMapping("/{ticketId}")
    public ResponseEntity<List<Attachments>> getAttachmentsByTicketId(@PathVariable String ticketId) {
        Tickets ticket = ticketService.findByTicketId(ticketId);
        List<Attachments> attachments = attachmentsService.getAttachmentsByTicketId(ticket); // ✅ Call service

        return ResponseEntity.ok(attachments);
    }
}
