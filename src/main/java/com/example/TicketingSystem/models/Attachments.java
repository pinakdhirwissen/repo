package com.example.TicketingSystem.models;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "uploads_attachments", indexes = {
        @Index(name = "idx_ticket_id", columnList = "ticketId")
})
@Getter
@Setter
@NoArgsConstructor
public class Attachments {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ticket_Id", referencedColumnName = "ticket_id")
    private Tickets ticketId;

    @Column()
    private String filePath; // Cloudinary URL

    @Column()
    private String typeOfFile;

    @Column()
    private String fileName;


}
