package com.asfak.employee_management_backend.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class MonthlyPayrollResponse {

    private Integer month;

    private String monthName;

    private Double amount;
}