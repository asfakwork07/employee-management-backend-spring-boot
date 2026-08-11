package com.asfak.employee_management_backend.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDailyBriefResponse {

    private LocalDate date;

    private long totalEmployees;

    private long presentToday;

    private long pendingLeaves;

    private double currentMonthPayroll;

    private String nextHoliday;

    private String summary;
}