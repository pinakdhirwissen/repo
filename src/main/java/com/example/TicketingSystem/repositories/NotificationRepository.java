package com.example.TicketingSystem.repositories;

import com.example.TicketingSystem.models.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

//    Finds unread notifications for a specific email ID.
    List<Notification> findByEmailIdAndIsReadFalse(String emailId);


    //    Deletes all notifications that have been marked as read.
    int deleteByIsReadTrue();
}