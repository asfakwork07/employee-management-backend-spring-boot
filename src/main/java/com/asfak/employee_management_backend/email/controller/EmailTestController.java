package com.asfak.employee_management_backend.email.controller;

import com.asfak.employee_management_backend.email.service.EmailService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
public class EmailTestController {

    private final EmailService emailService;

    @GetMapping("/test")
    public ResponseEntity<String> testEmail(
            @RequestParam String to
    ) {

        emailService.sendEmail(
                to,
                "EMS Email Test",
                "Hello,\n\n"
                        + "Your Employee Management System email notification is working successfully.\n\n"
                        + "Regards,\n"
                        + "Employee Management System"
        );

        return ResponseEntity.ok(
                "Email request processed successfully"
        );
    }
}