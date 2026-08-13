package com.asfak.employee_management_backend.ai.controller;

import com.asfak.employee_management_backend.ai.dto.PayrollExplanationResponse;
import com.asfak.employee_management_backend.ai.service.PayrollExplanationService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai/payroll")
@RequiredArgsConstructor
public class PayrollAiController {

    private final PayrollExplanationService
            payrollExplanationService;

    @GetMapping("/explain/{salaryId}")
    public ResponseEntity<PayrollExplanationResponse>
    explainSalary(
            @PathVariable Long salaryId,
            Authentication authentication
    ) {

        PayrollExplanationResponse response =
                payrollExplanationService
                        .explainSalary(
                                salaryId,
                                authentication.getName()
                        );

        return ResponseEntity.ok(
                response
        );
    }
}