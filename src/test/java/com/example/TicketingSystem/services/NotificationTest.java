package com.example.TicketingSystem.services;

import com.example.TicketingSystem.models.Notification;
import com.example.TicketingSystem.repositories.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class NotificationTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSendNotification_Success() {
        String emailId = "user@example.com";
        String message = "Test notification";
        Notification notification = new Notification(emailId, message, false, LocalDateTime.now());
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        notificationService.sendNotification(emailId, message);

        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(messagingTemplate, times(1)).convertAndSendToUser(eq(emailId), eq("/queue/notifications"), any(Notification.class));
    }

    @Test
    public void testSendNotification_NullEmail() {

        assertThrows(IllegalArgumentException.class, () -> notificationService.sendNotification(null, "Test message"));
        verify(notificationRepository, never()).save(any(Notification.class));
        verify(messagingTemplate, never()).convertAndSendToUser(anyString(), anyString(), any());
    }

    @Test
    public void testSendNotification_NullMessage() {
        assertThrows(IllegalArgumentException.class, () -> notificationService.sendNotification("user@example.com", null));
        verify(notificationRepository, never()).save(any(Notification.class));
        verify(messagingTemplate, never()).convertAndSendToUser(anyString(), anyString(), any());
    }

    @Test
    public void testMarkAsRead_Success() {
        Long id = 1L;
        Notification notification = new Notification("user@example.com", "Test message", false, LocalDateTime.now());
        when(notificationRepository.findById(id)).thenReturn(Optional.of(notification));

        boolean result = notificationService.markAsRead(id);

        assertTrue(result);
        assertTrue(notification.getIsRead());
        verify(notificationRepository, times(1)).save(notification);
    }

    @Test
    public void testMarkAsRead_NotificationNotFound() {

        Long id = 1L;
        when(notificationRepository.findById(id)).thenReturn(Optional.empty());


        boolean result = notificationService.markAsRead(id);

        assertFalse(result);
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    public void testDeleteReadNotificationsEveryMinute() {
        notificationService.deleteReadNotificationsEveryMinute();


        verify(notificationRepository, times(1)).deleteByIsReadTrue();
    }
}