package com.example.TicketingSystem.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;


//Represents a notification entity in the system.

@Entity
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String emailId;
    private String message;
    private boolean isRead;
    private LocalDateTime timestamp;

    public Notification() {
    }

//    Constructor to initialize all fields
    public Notification(String emailId, String message, boolean isRead, LocalDateTime timestamp) {
        this.emailId = emailId;
        this.message = message;
        this.isRead = isRead;
        this.timestamp = timestamp;
    }

    // ✅ Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(boolean isRead) {
        this.isRead = isRead;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}