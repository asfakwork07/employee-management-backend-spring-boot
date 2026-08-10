package com.asfak.employee_management_backend.dashboard.service;

import com.asfak.employee_management_backend.dashboard.dto.DashboardResponse;

public interface DashboardService {

    DashboardResponse getDashboard(
            String loggedInEmail
    );
}