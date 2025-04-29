package com.example.TicketingSystem.controllers;

import com.example.TicketingSystem.models.Notification;
import com.example.TicketingSystem.repositories.NotificationRepository;
import com.example.TicketingSystem.services.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService; // ✅ Inject NotificationService

    public NotificationController(NotificationRepository notificationRepository, NotificationService notificationService) {
        this.notificationRepository = notificationRepository;
        this.notificationService = notificationService;
    }

    // ✅ Get unread notifications for a user
    @CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
    @GetMapping("/{emailId}")
    public ResponseEntity<List<Notification>> getUnreadNotifications(@PathVariable String emailId) {
        List<Notification> unreadNotifications = notificationRepository.findByEmailIdAndIsReadFalse(emailId);
        System.out.println("Unread Notifications: " + unreadNotifications); // Log the data
        return ResponseEntity.ok(unreadNotifications); // ✅
    }

    @PutMapping("/read/{id}")
    public ResponseEntity<Map<String, String>> markNotificationAsRead(@PathVariable Long id) {
        boolean updated = notificationService.markAsRead(id);

        if (updated) {
            // Returning a JSON response with a message
            Map<String, String> response = new HashMap<>();
            response.put("message", "Notification marked as read.");
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // ✅ Mark notifications as read
    @PostMapping("/{emailId}/mark-read")
    public void markNotificationsAsRead(@PathVariable String emailId) {
        List<Notification> notifications = notificationRepository.findByEmailIdAndIsReadFalse(emailId);
        notifications.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(notifications);
    }

    // ✅ New endpoint to create a notification
    @PostMapping("/{emailId}/send")
    public void sendNotification(@PathVariable String emailId, @RequestBody String message) {
        notificationService.sendNotification(emailId, message); // ✅ Call NotificationService
    }
}