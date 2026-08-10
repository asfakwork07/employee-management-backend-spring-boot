package com.asfak.employee_management_backend.ai.service.impl;

import com.asfak.employee_management_backend.ai.dto.PerformanceSummaryResponse;
import com.asfak.employee_management_backend.ai.entity.PerformanceSummary;
import com.asfak.employee_management_backend.ai.repository.PerformanceSummaryRepository;
import com.asfak.employee_management_backend.ai.service.AiService;
import com.asfak.employee_management_backend.ai.service.PerformanceSummaryService;

import com.asfak.employee_management_backend.attendance.entity.Attendance;
import com.asfak.employee_management_backend.attendance.repository.AttendanceRepository;

import com.asfak.employee_management_backend.entity.Employee;

import com.asfak.employee_management_backend.leave.entity.LeaveRequest;
import com.asfak.employee_management_backend.leave.repository.LeaveRequestRepository;

import com.asfak.employee_management_backend.repository.EmployeeRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PerformanceSummaryServiceImpl
        implements PerformanceSummaryService {

    private final EmployeeRepository employeeRepository;

    private final AttendanceRepository attendanceRepository;

    private final LeaveRequestRepository leaveRequestRepository;

    private final AiService aiService;

    private final PerformanceSummaryRepository performanceSummaryRepository;

    @Override
    public PerformanceSummaryResponse generatePerformanceSummary(
            Long employeeId,
            Integer month,
            Integer year,
            boolean forceRegenerate
    ) {

        validateMonthYear(
                month,
                year
        );

        Employee employee =
                employeeRepository
                        .findById(
                                employeeId
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Employee not found"
                                )
                        );

        YearMonth selectedMonth =
                YearMonth.of(
                        year,
                        month
                );

        LocalDate startDate =
                selectedMonth.atDay(1);

        LocalDate endDate =
                selectedMonth.atEndOfMonth();

        List<Attendance> attendanceList =
                attendanceRepository
                        .findByEmployeeIdAndAttendanceDateBetween(
                                employeeId,
                                startDate,
                                endDate
                        );

        int presentDays = 0;

        int lateDays = 0;

        int halfDays = 0;

        int shortHoursDays = 0;

        double totalWorkingHours = 0.0;

        for (
                Attendance attendance :
                attendanceList
        ) {

            String status =
                    attendance.getStatus() == null
                            ? ""
                            : attendance
                            .getStatus()
                            .trim()
                            .toUpperCase();

            switch (status) {

                case "PRESENT" -> {

                    presentDays++;
                }

                case "LATE" -> {

                    lateDays++;
                }

                case "HALF_DAY" -> {

                    halfDays++;
                }

                case "SHORT_HOURS" -> {

                    shortHoursDays++;
                }

                default -> {
                }
            }

            if (
                    attendance.getTotalHours() != null
            ) {

                totalWorkingHours +=
                        attendance.getTotalHours();
            }
        }

        List<LeaveRequest> approvedLeaves =
                leaveRequestRepository
                        .findByEmployeeIdAndStatusAndToDateGreaterThanEqualAndFromDateLessThanEqual(
                                employeeId,
                                "APPROVED",
                                startDate,
                                endDate
                        );

        int approvedLeaveDays =
                calculateApprovedLeaveDays(
                        approvedLeaves,
                        startDate,
                        endDate
                );

        int attendanceRecords =
                attendanceList.size();

        double averageWorkingHours =
                calculateAverageWorkingHours(
                        attendanceList
                );

        String prompt =
                buildPerformancePrompt(
                        employee,
                        month,
                        year,
                        presentDays,
                        lateDays,
                        halfDays,
                        shortHoursDays,
                        approvedLeaveDays,
                        attendanceRecords,
                        totalWorkingHours,
                        averageWorkingHours
                );

        Optional<PerformanceSummary> existingSummary =
                performanceSummaryRepository
                        .findByEmployeeIdAndMonthAndYear(
                                employeeId,
                                month,
                                year
                        );

        String aiSummary;

        if (
                existingSummary.isPresent()
                        &&
                        !forceRegenerate
        ) {

            aiSummary =
                    existingSummary
                            .get()
                            .getSummary();

        } else {

            String generatedSummary =
                    aiService.generateText(
                            prompt
                    );

            if (
                    generatedSummary == null
                            ||
                            generatedSummary.isBlank()
            ) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "AI summary could not be generated"
                );
            }

            if (
                    existingSummary.isPresent()
            ) {

                PerformanceSummary summary =
                        existingSummary.get();

                summary.setSummary(
                        generatedSummary
                );

                summary.setGeneratedAt(
                        LocalDateTime.now()
                );

                performanceSummaryRepository
                        .save(
                                summary
                        );

            } else {

                PerformanceSummary newSummary =
                        PerformanceSummary
                                .builder()
                                .employee(
                                        employee
                                )
                                .month(
                                        month
                                )
                                .year(
                                        year
                                )
                                .summary(
                                        generatedSummary
                                )
                                .generatedAt(
                                        LocalDateTime.now()
                                )
                                .build();

                performanceSummaryRepository
                        .save(
                                newSummary
                        );
            }

            aiSummary =
                    generatedSummary;
        }

        return PerformanceSummaryResponse
                .builder()
                .employeeId(
                        employee.getId()
                )
                .employeeName(
                        employee.getFirstName()
                                + " "
                                + employee.getLastName()
                )
                .department(
                        employee.getDepartment()
                )
                .designation(
                        employee.getDesignation()
                )
                .month(
                        month
                )
                .year(
                        year
                )
                .presentDays(
                        presentDays
                )
                .lateDays(
                        lateDays
                )
                .halfDays(
                        halfDays
                )
                .shortHoursDays(
                        shortHoursDays
                )
                .approvedLeaveDays(
                        approvedLeaveDays
                )
                .attendanceRecords(
                        attendanceRecords
                )
                .totalWorkingHours(
                        round(
                                totalWorkingHours
                        )
                )
                .averageWorkingHours(
                        round(
                                averageWorkingHours
                        )
                )
                .summary(
                        aiSummary
                )
                .build();
    }

    private int calculateApprovedLeaveDays(
            List<LeaveRequest> leaves,
            LocalDate monthStart,
            LocalDate monthEnd
    ) {

        int totalDays = 0;

        for (
                LeaveRequest leave :
                leaves
        ) {

            if (
                    leave.getFromDate() == null
                            ||
                            leave.getToDate() == null
                            ||
                            leave.getTotalDays() == null
            ) {

                continue;
            }

            if (
                    leave.getFromDate()
                            .isBefore(
                                    monthStart
                            )
                            ||
                            leave.getToDate()
                                    .isAfter(
                                            monthEnd
                                    )
            ) {

                LocalDate start =
                        leave.getFromDate()
                                .isBefore(
                                        monthStart
                                )
                                ? monthStart
                                : leave.getFromDate();

                LocalDate end =
                        leave.getToDate()
                                .isAfter(
                                        monthEnd
                                )
                                ? monthEnd
                                : leave.getToDate();

                long calendarDays =
                        ChronoUnit.DAYS
                                .between(
                                        start,
                                        end
                                )
                                + 1;

                long originalCalendarDays =
                        ChronoUnit.DAYS
                                .between(
                                        leave.getFromDate(),
                                        leave.getToDate()
                                )
                                + 1;

                if (
                        originalCalendarDays > 0
                ) {

                    double ratio =
                            (double) calendarDays
                                    /
                                    originalCalendarDays;

                    totalDays +=
                            (int) Math.round(
                                    leave.getTotalDays()
                                            * ratio
                            );
                }

            } else {

                totalDays +=
                        leave.getTotalDays();
            }
        }

        return totalDays;
    }

    private double calculateAverageWorkingHours(
            List<Attendance> attendanceList
    ) {

        double totalHours = 0.0;

        int recordsWithHours = 0;

        for (
                Attendance attendance :
                attendanceList
        ) {

            if (
                    attendance.getTotalHours() != null
                            &&
                            attendance.getTotalHours() > 0
            ) {

                totalHours +=
                        attendance.getTotalHours();

                recordsWithHours++;
            }
        }

        if (
                recordsWithHours == 0
        ) {

            return 0.0;
        }

        return totalHours
                /
                recordsWithHours;
    }

    private String buildPerformancePrompt(
            Employee employee,
            Integer month,
            Integer year,
            int presentDays,
            int lateDays,
            int halfDays,
            int shortHoursDays,
            int approvedLeaveDays,
            int attendanceRecords,
            double totalWorkingHours,
            double averageWorkingHours
    ) {

        return """
                You are generating a factual monthly employee activity summary.
                
                IMPORTANT RULES:
                - Use only the data provided below.
                - Do not guess missing information.
                - Do not rank the employee.
                - Do not give a performance score.
                - Do not recommend promotion, termination, salary increase, disciplinary action, or any employment decision.
                - Do not describe the employee as high-performing, low-performing, good, bad, excellent, or poor.
                - Keep the summary neutral and professional.
                - Mention notable attendance patterns only when supported by the provided numbers.
                - Write 3 to 5 concise sentences.
                
                Employee:
                Name: %s %s
                Department: %s
                Designation: %s
                
                Period:
                Month: %d
                Year: %d
                
                Attendance data:
                Attendance records: %d
                Present days: %d
                Late days: %d
                Half days: %d
                Short-hours days: %d
                
                Leave data:
                Approved leave days: %d
                
                Working-hours data:
                Total working hours: %.2f
                Average recorded working hours: %.2f
                
                Generate the factual monthly activity summary now.
                """
                .formatted(
                        employee.getFirstName(),
                        employee.getLastName(),
                        employee.getDepartment(),
                        employee.getDesignation(),
                        month,
                        year,
                        attendanceRecords,
                        presentDays,
                        lateDays,
                        halfDays,
                        shortHoursDays,
                        approvedLeaveDays,
                        totalWorkingHours,
                        averageWorkingHours
                );
    }

    private void validateMonthYear(
            Integer month,
            Integer year
    ) {

        if (
                month == null
                        ||
                        month < 1
                        ||
                        month > 12
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Month must be between 1 and 12"
            );
        }

        if (
                year == null
                        ||
                        year < 2000
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid year"
            );
        }
    }

    private double round(
            double value
    ) {

        return Math.round(
                value * 100.0
        ) / 100.0;
    }
}