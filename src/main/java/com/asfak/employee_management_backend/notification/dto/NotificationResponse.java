package com.asfak.employee_management_backend.notification.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {

    private Long id;

    private String title;

    private String message;

    private String type;

    private Boolean read;

    private LocalDateTime createdAt;
}