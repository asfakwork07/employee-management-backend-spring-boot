package com.asfak.employee_management_backend.notification.entity;

import com.asfak.employee_management_backend.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String title;

    @Column(length = 1000)
    private String message;

    private String type;

    private Boolean readStatus;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {

        if (readStatus == null) {
            readStatus = false;
        }

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}