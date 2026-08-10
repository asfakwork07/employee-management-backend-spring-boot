package com.asfak.employee_management_backend.leave.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ApplyLeaveRequest {

    @NotNull
    private Long employeeId;

    @NotNull
    private Long leaveTypeId;

    @NotNull
    @FutureOrPresent
    private LocalDate fromDate;

    @NotNull
    @FutureOrPresent
    private LocalDate toDate;

    @NotBlank
    private String reason;

}