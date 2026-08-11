package com.asfak.employee_management_backend.ai.controller;

import com.asfak.employee_management_backend.ai.dto.AdminDailyBriefResponse;
import com.asfak.employee_management_backend.ai.service.AdminDailyBriefService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai/admin")
@RequiredArgsConstructor
public class AdminAiBriefController {

    private final AdminDailyBriefService adminDailyBriefService;

    @GetMapping("/daily-brief")
    public ResponseEntity<AdminDailyBriefResponse>
    getDailyBrief() {

        return ResponseEntity.ok(
                adminDailyBriefService
                        .generateDailyBrief()
        );
    }
}