package com.asfak.employee_management_backend.notification.controller;

import com.asfak.employee_management_backend.notification.dto.NotificationResponse;
import com.asfak.employee_management_backend.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                notificationService.getMyNotifications(
                        authentication.getName()
                )
        );
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            Authentication authentication
    ) {

        long count =
                notificationService.getUnreadCount(
                        authentication.getName()
                );

        return ResponseEntity.ok(
                Map.of(
                        "unreadCount",
                        count
                )
        );
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Map<String, String>> markAsRead(
            @PathVariable Long notificationId,
            Authentication authentication
    ) {

        notificationService.markAsRead(
                notificationId,
                authentication.getName()
        );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Notification marked as read"
                )
        );
    }

    @PutMapping("/read-all")
    public ResponseEntity<Map<String, String>> markAllAsRead(
            Authentication authentication
    ) {

        notificationService.markAllAsRead(
                authentication.getName()
        );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "All notifications marked as read"
                )
        );
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Map<String, String>>
    deleteNotification(
            @PathVariable Long notificationId,
            Authentication authentication
    ) {

        notificationService
                .deleteNotification(
                        notificationId,
                        authentication.getName()
                );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Notification deleted successfully"
                )
        );
    }

    @DeleteMapping("/clear-all")
    public ResponseEntity<Map<String, String>>
    clearAllNotifications(
            Authentication authentication
    ) {

        notificationService
                .clearAllNotifications(
                        authentication.getName()
                );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "All notifications cleared successfully"
                )
        );
    }
}