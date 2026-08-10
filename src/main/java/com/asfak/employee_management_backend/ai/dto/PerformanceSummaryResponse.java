package com.asfak.employee_management_backend.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceSummaryResponse {

    private Long employeeId;

    private String employeeName;

    private String department;

    private String designation;

    private Integer month;

    private Integer year;

    private Integer presentDays;

    private Integer lateDays;

    private Integer halfDays;

    private Integer shortHoursDays;

    private Integer approvedLeaveDays;

    private Integer attendanceRecords;

    private Double totalWorkingHours;

    private Double averageWorkingHours;

    private String summary;
}