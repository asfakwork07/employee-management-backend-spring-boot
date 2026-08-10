package com.asfak.employee_management_backend.notification.repository;

import com.asfak.employee_management_backend.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(
            Long userId
    );

    long countByUserIdAndReadStatusFalse(
            Long userId
    );

    void deleteByUserId(Long userId);

    void deleteByIdAndUserId(
            Long notificationId,
            Long userId
    );
}