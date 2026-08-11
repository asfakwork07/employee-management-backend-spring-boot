package com.asfak.employee_management_backend.ai.service.impl;

import com.asfak.employee_management_backend.ai.dto.AdminDailyBriefResponse;
import com.asfak.employee_management_backend.ai.service.AdminDailyBriefService;
import com.asfak.employee_management_backend.ai.service.AiService;

import com.asfak.employee_management_backend.attendance.repository.AttendanceRepository;

import com.asfak.employee_management_backend.holiday.entity.Holiday;
import com.asfak.employee_management_backend.holiday.repository.HolidayRepository;

import com.asfak.employee_management_backend.leave.repository.LeaveRequestRepository;

import com.asfak.employee_management_backend.repository.EmployeeRepository;

import com.asfak.employee_management_backend.salary.repository.SalaryRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDailyBriefServiceImpl
        implements AdminDailyBriefService {

    private final EmployeeRepository employeeRepository;

    private final AttendanceRepository attendanceRepository;

    private final LeaveRequestRepository leaveRequestRepository;

    private final SalaryRepository salaryRepository;

    private final HolidayRepository holidayRepository;

    private final AiService aiService;

    @Override
    public AdminDailyBriefResponse generateDailyBrief() {

        LocalDate today =
                LocalDate.now();

        long totalEmployees =
                employeeRepository.count();

        long presentToday =
                attendanceRepository
                        .countByAttendanceDate(
                                today
                        );

        long pendingLeaves =
                leaveRequestRepository
                        .countByStatus(
                                "PENDING"
                        );

        Double payroll =
                salaryRepository
                        .getMonthlyPayroll(
                                today.getMonthValue(),
                                today.getYear()
                        );

        if (
                payroll == null
        ) {
            payroll = 0.0;
        }

        List<Holiday> upcomingHolidays =
                holidayRepository
                        .findByHolidayDateGreaterThanEqualOrderByHolidayDateAsc(
                                today
                        );

        Holiday nextHoliday =
                upcomingHolidays
                        .stream()
                        .findFirst()
                        .orElse(null);

        String nextHolidayText =
                nextHoliday == null
                        ? "No upcoming holiday configured"
                        : nextHoliday.getName()
                          + " on "
                          + nextHoliday.getHolidayDate();

        String prompt =
                buildPrompt(
                        today,
                        totalEmployees,
                        presentToday,
                        pendingLeaves,
                        payroll,
                        nextHolidayText
                );

        String summary =
                aiService.generateText(
                        prompt
                );

        return AdminDailyBriefResponse
                .builder()
                .date(
                        today
                )
                .totalEmployees(
                        totalEmployees
                )
                .presentToday(
                        presentToday
                )
                .pendingLeaves(
                        pendingLeaves
                )
                .currentMonthPayroll(
                        payroll
                )
                .nextHoliday(
                        nextHolidayText
                )
                .summary(
                        summary
                )
                .build();
    }

    private String buildPrompt(
            LocalDate today,
            long totalEmployees,
            long presentToday,
            long pendingLeaves,
            double payroll,
            String nextHoliday
    ) {

        return """
                You are generating a factual daily HR operations brief
                for an administrator inside an Employee Management System.

                IMPORTANT RULES:
                - Use only the verified data provided below.
                - Do not invent missing information.
                - Do not rank employees.
                - Do not make hiring, firing, promotion,
                  salary increase, disciplinary, or employment decisions.
                - Keep the brief professional and concise.
                - Write 3 to 5 sentences.
                - Mention only operational facts and notable counts.

                Date:
                %s

                Organization data:
                Total employees: %d
                Employees present today: %d
                Pending leave requests: %d
                Current month payroll: %.2f
                Next holiday: %s

                Generate the daily admin brief now.
                """
                .formatted(
                        today,
                        totalEmployees,
                        presentToday,
                        pendingLeaves,
                        payroll,
                        nextHoliday
                );
    }
}