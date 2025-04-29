package com.example.TicketingSystem.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "feedback")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    // Establish Foreign Key Relationship with Ticket
    @ManyToOne
    @JoinColumn(name = "ticket_Id", referencedColumnName = "ticket_id")
    private Tickets ticketId;

    private String assigneeEmail;
    private String createdBy;
    private String rating; // Example: 1-5 stars
    private String comments;
    private LocalDateTime submittedAt = LocalDateTime.now();
}