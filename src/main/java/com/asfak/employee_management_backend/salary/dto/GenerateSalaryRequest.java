package com.asfak.employee_management_backend.salary.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GenerateSalaryRequest {

    @NotNull
    private Long employeeId;

    @NotNull
    private Integer salaryMonth;

    @NotNull
    private Integer salaryYear;
}