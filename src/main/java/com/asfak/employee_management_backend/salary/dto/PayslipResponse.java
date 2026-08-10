package com.asfak.employee_management_backend.salary.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PayslipResponse {

    private String employeeName;

    private String department;

    private String designation;

    private Integer salaryMonth;

    private Integer salaryYear;

    private BigDecimal basicSalary;

    private BigDecimal hra;

    private BigDecimal allowance;

    private BigDecimal pf;

    private BigDecimal professionalTax;

    private BigDecimal incomeTax;

    private BigDecimal grossSalary;

    private BigDecimal netSalary;
}