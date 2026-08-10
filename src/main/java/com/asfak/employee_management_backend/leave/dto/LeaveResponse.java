package com.asfak.employee_management_backend.leave.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class LeaveResponse {

    private Long id;

    private String employeeName;

    private String leaveType;

    private LocalDate fromDate;

    private LocalDate toDate;

    private Integer totalDays;

    private String status;

}