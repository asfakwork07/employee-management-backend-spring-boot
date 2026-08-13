package com.asfak.employee_management_backend.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayrollExplanationResponse {

    private Long salaryId;

    private String employeeName;

    private Integer salaryMonth;

    private Integer salaryYear;

    private BigDecimal basicSalary;

    private BigDecimal hra;

    private BigDecimal allowance;

    private BigDecimal grossSalary;

    private BigDecimal pf;

    private BigDecimal professionalTax;

    private BigDecimal incomeTax;

    private BigDecimal totalDeductions;

    private BigDecimal netSalary;

    private String explanation;
}