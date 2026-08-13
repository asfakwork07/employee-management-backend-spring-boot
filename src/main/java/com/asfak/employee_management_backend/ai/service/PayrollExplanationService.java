package com.asfak.employee_management_backend.ai.service;

import com.asfak.employee_management_backend.ai.dto.PayrollExplanationResponse;

public interface PayrollExplanationService {

    PayrollExplanationResponse explainSalary(
            Long salaryId,
            String loggedInEmail
    );
}