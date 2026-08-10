package com.asfak.employee_management_backend.notification.service.impl;

import com.asfak.employee_management_backend.entity.User;
import com.asfak.employee_management_backend.notification.dto.NotificationResponse;
import com.asfak.employee_management_backend.notification.entity.Notification;
import com.asfak.employee_management_backend.notification.repository.NotificationRepository;
import com.asfak.employee_management_backend.notification.service.NotificationService;
import com.asfak.employee_management_backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    public void createNotification(
            User user,
            String title,
            String message,
            String type
    ) {

        if (user == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Notification user cannot be null"
            );
        }

        Notification notification =
                Notification.builder()
                        .user(user)
                        .title(title)
                        .message(message)
                        .type(type)
                        .readStatus(false)
                        .build();

        notificationRepository.save(notification);
    }

    @Override
    public List<NotificationResponse> getMyNotifications(
            String loggedInEmail
    ) {

        User user = getLoggedInUser(loggedInEmail);

        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public long getUnreadCount(
            String loggedInEmail
    ) {

        User user = getLoggedInUser(loggedInEmail);

        return notificationRepository
                .countByUserIdAndReadStatusFalse(user.getId());
    }

    @Override
    @Transactional
    public void markAsRead(
            Long notificationId,
            String loggedInEmail
    ) {

        User user = getLoggedInUser(loggedInEmail);

        Notification notification =
                notificationRepository
                        .findById(notificationId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Notification not found"
                                )
                        );

        if (!notification.getUser().getId().equals(user.getId())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot access this notification"
            );
        }

        notification.setReadStatus(true);

        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(
            String loggedInEmail
    ) {

        User user = getLoggedInUser(loggedInEmail);

        List<Notification> notifications =
                notificationRepository
                        .findByUserIdOrderByCreatedAtDesc(user.getId());

        notifications.forEach(notification ->
                notification.setReadStatus(true)
        );

        notificationRepository.saveAll(notifications);
    }

    private User getLoggedInUser(
            String email
    ) {

        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Logged-in user not found"
                        )
                );
    }

    private NotificationResponse toResponse(
            Notification notification
    ) {

        return NotificationResponse
                .builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .read(notification.getReadStatus())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public void deleteNotification(
            Long notificationId,
            String loggedInEmail
    ) {

        User user =
                getLoggedInUser(
                        loggedInEmail
                );

        Notification notification =
                notificationRepository
                        .findById(notificationId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Notification not found"
                                )
                        );

        if (
                !notification
                        .getUser()
                        .getId()
                        .equals(user.getId())
        ) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot delete this notification"
            );
        }

        notificationRepository.delete(
                notification
        );
    }

    @Override
    @Transactional
    public void clearAllNotifications(
            String loggedInEmail
    ) {

        User user =
                getLoggedInUser(
                        loggedInEmail
                );

        notificationRepository
                .deleteByUserId(
                        user.getId()
                );
    }
}