package com.asfak.employee_management_backend.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class DashboardResponse {

    private String role;

    private Long totalEmployees;

    private Long presentToday;

    private Long absentToday;

    private Long pendingLeaves;

    private Double monthlyPayroll;

    private Long employeeId;

    private String employeeName;

    private String attendanceStatus;

    private String checkIn;

    private String checkOut;

    private Double workingHours;

    private Long myPendingLeaves;

    private Double latestNetSalary;

    private Map<String, Long> employeesByDepartment;

    private Map<String, Long> leaveStatusStats;

    private List<MonthlyPayrollResponse> monthlyPayrollStats;
}