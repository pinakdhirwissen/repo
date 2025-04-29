package com.example.TicketingSystem.services;

import com.example.TicketingSystem.models.Notification;
import com.example.TicketingSystem.repositories.NotificationRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);


    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(NotificationRepository notificationRepository, SimpMessagingTemplate messagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.messagingTemplate = messagingTemplate;
    }

//    Sends a notification to a user and saves it in the database.
    public void sendNotification(String emailId, String message) {
        if (emailId == null || message == null) {
            throw new IllegalArgumentException("Email and message must not be null");
        }
        Notification notification = new Notification(emailId, message, false, LocalDateTime.now());
        notificationRepository.save(notification);

        messagingTemplate.convertAndSendToUser(emailId, "/queue/notifications", notification);
        logger.info("Notification sent to user: {}", emailId);
    }


//    Deletes all read notifications daily at midnight.
    @Transactional
    @Scheduled(cron = "0 0 0 * * ?")
    public void deleteReadNotificationsEveryMinute() {
        int deletedCount = notificationRepository.deleteByIsReadTrue();
        logger.info("Deleted {} read notifications.", deletedCount);
    }


//    Mark all Notification as read
    public boolean markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElse(null);
        if (notification == null) {
            return false;
        }
            notification.setIsRead(true);
            notificationRepository.save(notification);
            return true;

    }

}