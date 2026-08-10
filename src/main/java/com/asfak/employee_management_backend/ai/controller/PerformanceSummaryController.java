package com.asfak.employee_management_backend.ai.controller;

import com.asfak.employee_management_backend.ai.dto.PerformanceSummaryResponse;
import com.asfak.employee_management_backend.ai.service.PerformanceSummaryService;
import com.asfak.employee_management_backend.entity.User;
import com.asfak.employee_management_backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/ai/performance")
@RequiredArgsConstructor
public class PerformanceSummaryController {

    private final PerformanceSummaryService performanceSummaryService;

    private final UserRepository userRepository;

    // =========================================================
    // ADMIN - GET ANY EMPLOYEE PERFORMANCE SUMMARY
    // =========================================================

    @GetMapping("/{employeeId}")
    public ResponseEntity<PerformanceSummaryResponse>
    getPerformanceSummary(

            @PathVariable Long employeeId,

            @RequestParam Integer month,

            @RequestParam Integer year,

            @RequestParam(
                    defaultValue = "false"
            )
            boolean regenerate
    ) {

        return ResponseEntity.ok(
                performanceSummaryService
                        .generatePerformanceSummary(
                                employeeId,
                                month,
                                year,
                                regenerate
                        )
        );
    }

    // =========================================================
    // EMPLOYEE - GET OWN PERFORMANCE SUMMARY
    // =========================================================

    @GetMapping("/me")
    public ResponseEntity<PerformanceSummaryResponse>
    getMyPerformanceSummary(

            @RequestParam Integer month,

            @RequestParam Integer year,

            @RequestParam(
                    defaultValue = "false"
            )
            boolean regenerate,

            Authentication authentication
    ) {

        if (
                authentication == null ||
                        authentication.getName() == null
        ) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User is not authenticated"
            );
        }

        String loggedInEmail =
                authentication.getName();

        User user =
                userRepository
                        .findByEmail(
                                loggedInEmail
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "Logged-in user not found"
                                )
                        );

        if (
                user.getEmployee() == null
        ) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "User account is not linked with an employee"
            );
        }

        Long employeeId =
                user.getEmployee()
                        .getId();

        return ResponseEntity.ok(
                performanceSummaryService
                        .generatePerformanceSummary(
                                employeeId,
                                month,
                                year,
                                regenerate
                        )
        );
    }
}