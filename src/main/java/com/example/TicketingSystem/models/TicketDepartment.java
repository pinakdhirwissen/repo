package com.example.TicketingSystem.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Ticket_Department")
@Getter
@Setter
@NoArgsConstructor
public class TicketDepartment {

    @Id
    @Column(name = "id", length = 100, nullable = false)
    private String id;

    @Column(name = "department", length = 50, nullable = false)
    private String department;

    @Column(name = "email_id", length = 100, nullable = false, unique = true)
    private String emailId;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}
