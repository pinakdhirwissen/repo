package com.example.TicketingSystem.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.time.Instant;

@Entity
@Table(name = "Tickets")
@Getter
@Setter
@NoArgsConstructor
public class Tickets {

    @Id
    @Column(name = "ticket_id", length = 50, nullable = false, updatable = false, unique = true)
    private String ticketId;

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must be less than 200 characters")
    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Size(max = 1000, message = "Description must be less than 1000 characters")
    @Column(name = "description", length = 1000)
    private String description;

    @Size(max = 50)
    @Column(name = "department", length = 50)
    private String department;

    @Size(max = 100)
    @Column(name = "assign_to", length = 100)
    private String assignTo;

    @Size(max = 100)
    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Size(max = 100)
    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Size(max = 30)
    @Column(name = "priority", length = 30)
    private String priority;
    @Column(name = "due_date")
    private LocalDate dueDate;
    @Column(name = "status", length = 20, nullable = false)
    private String status = "Open"; // Default value

    // Automatically set the creation date and requestId when persisting
    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
        if (this.ticketId == null || this.ticketId.isBlank()) {
            this.ticketId = generateRequestId();
        }
    }

    // Automatically update the updatedDate when merging
    @PreUpdate
    protected void onUpdate() {
        this.updatedDate = LocalDateTime.now();
    }
    private String generateRequestId() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        return "REQ" + LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()).format(formatter);
    }
}
