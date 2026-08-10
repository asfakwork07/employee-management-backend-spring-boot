package com.asfak.employee_management_backend.email.service.impl;

import com.asfak.employee_management_backend.email.service.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Override
    public void sendEmail(
            String to,
            String subject,
            String message
    ) {

        try {

            SimpleMailMessage mailMessage =
                    new SimpleMailMessage();

            mailMessage.setFrom(
                    senderEmail
            );

            mailMessage.setTo(
                    to
            );

            mailMessage.setSubject(
                    subject
            );

            mailMessage.setText(
                    message
            );

            mailSender.send(
                    mailMessage
            );

            log.info(
                    "Email sent successfully to: {}",
                    to
            );

        } catch (Exception e) {

            log.error(
                    "Unable to send email to: {}",
                    to,
                    e
            );
        }
    }
}