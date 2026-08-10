package com.asfak.employee_management_backend.notification.service;

import com.asfak.employee_management_backend.entity.User;
import com.asfak.employee_management_backend.notification.dto.NotificationResponse;

import java.util.List;

public interface NotificationService {

    void createNotification(
            User user,
            String title,
            String message,
            String type
    );

    List<NotificationResponse> getMyNotifications(
            String loggedInEmail
    );

    long getUnreadCount(
            String loggedInEmail
    );

    void markAsRead(
            Long notificationId,
            String loggedInEmail
    );

    void markAllAsRead(
            String loggedInEmail
    );

    void deleteNotification(
            Long notificationId,
            String loggedInEmail
    );

    void clearAllNotifications(
            String loggedInEmail
    );
}