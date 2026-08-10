package com.asfak.employee_management_backend.email.service;

public interface EmailService {

    void sendEmail(
            String to,
            String subject,
            String message
    );
}