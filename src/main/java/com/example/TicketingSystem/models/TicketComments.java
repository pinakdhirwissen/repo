package com.example.TicketingSystem.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_comments")
@Getter
@Setter
@NoArgsConstructor
public class TicketComments {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto-generate primary key
    @Column(name = "comment_id", updatable = false, nullable = false)
    private Long commentId;

    @NotBlank(message = "Comment is required")
    @Column(name = "comment", length = 1000, nullable = false)
    private String comment;

    @Column(name = "commented_by", length = 100, nullable = false)
    private String commentedBy;

    @Column(name = "commented_date", nullable = false, updatable = false)
    private LocalDateTime commentedDate;

    @Column(name = "edited_date")
    private LocalDateTime editedDate;

    @Column(name = "is_edited", nullable = false)
    private boolean isEdited = false;

    // One ticket can have multiple comments
    @ManyToOne
    @JoinColumn(name = "ticket_id", referencedColumnName = "ticket_id", nullable = false)
    private Tickets ticketId;

    @PrePersist
    protected void onCreate() {
        if (this.commentedDate == null) {
            this.commentedDate = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        if (this.isEdited) {
            this.editedDate = LocalDateTime.now();
        }
    }

   
}
