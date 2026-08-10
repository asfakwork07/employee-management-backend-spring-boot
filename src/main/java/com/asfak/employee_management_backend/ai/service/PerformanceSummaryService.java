package com.asfak.employee_management_backend.ai.service;

import com.asfak.employee_management_backend.ai.dto.PerformanceSummaryResponse;

public interface PerformanceSummaryService {

    PerformanceSummaryResponse generatePerformanceSummary(
            Long employeeId,
            Integer month,
            Integer year,
            boolean forceRegenerate
    );
}