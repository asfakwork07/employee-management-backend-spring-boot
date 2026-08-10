package com.asfak.employee_management_backend.dashboard.controller;

import com.asfak.employee_management_backend.dashboard.dto.DashboardResponse;
import com.asfak.employee_management_backend.dashboard.service.DashboardService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public DashboardResponse getDashboard(
            Authentication authentication
    ) {

        return dashboardService.getDashboard(
                authentication.getName()
        );
    }
}