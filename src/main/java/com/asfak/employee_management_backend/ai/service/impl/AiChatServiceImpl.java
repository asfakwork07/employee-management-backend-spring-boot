package com.asfak.employee_management_backend.ai.service.impl;

import com.asfak.employee_management_backend.ai.dto.AiChatRequest;
import com.asfak.employee_management_backend.ai.dto.AiChatResponse;
import com.asfak.employee_management_backend.ai.service.AiChatService;
import com.asfak.employee_management_backend.ai.service.AiService;

import com.asfak.employee_management_backend.attendance.entity.Attendance;
import com.asfak.employee_management_backend.attendance.repository.AttendanceRepository;

import com.asfak.employee_management_backend.entity.Employee;
import com.asfak.employee_management_backend.entity.User;

import com.asfak.employee_management_backend.holiday.entity.Holiday;
import com.asfak.employee_management_backend.holiday.repository.HolidayRepository;

import com.asfak.employee_management_backend.leave.entity.LeaveType;
import com.asfak.employee_management_backend.leave.repository.LeaveRequestRepository;
import com.asfak.employee_management_backend.leave.repository.LeaveTypeRepository;

import com.asfak.employee_management_backend.repository.EmployeeRepository;
import com.asfak.employee_management_backend.repository.UserRepository;

import com.asfak.employee_management_backend.salary.entity.Salary;
import com.asfak.employee_management_backend.salary.repository.SalaryRepository;

import com.asfak.employee_management_backend.settings.entity.SystemSettings;
import com.asfak.employee_management_backend.settings.repository.SystemSettingsRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AiChatServiceImpl
        implements AiChatService {

    private final UserRepository userRepository;

    private final EmployeeRepository employeeRepository;

    private final AttendanceRepository attendanceRepository;

    private final LeaveRequestRepository leaveRequestRepository;

    private final SalaryRepository salaryRepository;

    private final HolidayRepository holidayRepository;

    private final LeaveTypeRepository leaveTypeRepository;

    private final SystemSettingsRepository systemSettingsRepository;

    private final AiService aiService;

    @Override
    public AiChatResponse chat(
            AiChatRequest request,
            String loggedInEmail
    ) {

        User user =
                userRepository
                        .findByEmail(
                                loggedInEmail
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "Logged-in user not found"
                                )
                        );

        String role =
                user.getRole() == null
                        ? ""
                        : user.getRole()
                        .trim()
                        .toUpperCase();

        String question =
                request.getMessage() == null
                        ? ""
                        : request.getMessage()
                        .trim();

        String directAnswer =
                tryDirectAnswer(
                        question,
                        role,
                        user
                );

        if (directAnswer != null) {

            return buildResponse(
                    question,
                    directAnswer,
                    role
            );
        }

        String context;

        if (
                "ADMIN".equalsIgnoreCase(
                        role
                )
        ) {

            context =
                    buildAdminContext();

        } else {

            context =
                    buildEmployeeContext(
                            user
                    );
        }

        String prompt =
                buildPrompt(
                        question,
                        role,
                        context
                );

        String aiAnswer =
                aiService.generateText(
                        prompt
                );

        return buildResponse(
                question,
                aiAnswer,
                role
        );
    }

    // =========================================================
    // HYBRID DIRECT ANSWER ROUTER
    // =========================================================

    private String tryDirectAnswer(
            String question,
            String role,
            User user
    ) {

        String normalized =
                question
                        .toLowerCase(
                                Locale.ENGLISH
                        )
                        .trim();

        if (
                "ADMIN".equalsIgnoreCase(
                        role
                )
        ) {

            String adminAnswer =
                    tryAdminDirectAnswer(
                            normalized
                    );

            if (adminAnswer != null) {

                return adminAnswer;
            }
        }

        if (
                "EMPLOYEE".equalsIgnoreCase(
                        role
                )
        ) {

            String employeeAnswer =
                    tryEmployeeDirectAnswer(
                            normalized,
                            user
                    );

            if (employeeAnswer != null) {

                return employeeAnswer;
            }
        }

        String holidayAnswer =
                tryHolidayDirectAnswer(
                        normalized
                );

        if (holidayAnswer != null) {

            return holidayAnswer;
        }

        return null;
    }

    // =========================================================
    // EMPLOYEE DIRECT ANSWERS
    // =========================================================

    private String tryEmployeeDirectAnswer(
            String question,
            User user
    ) {

        if (
                user.getEmployee() == null
        ) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "User account is not linked with an employee"
            );
        }

        Employee employee =
                user.getEmployee();

        Long employeeId =
                employee.getId();

        LocalDate today =
                LocalDate.now();

        YearMonth currentMonth =
                YearMonth.from(
                        today
                );

        LocalDate monthStart =
                currentMonth.atDay(1);

        LocalDate monthEnd =
                currentMonth.atEndOfMonth();

        // =====================================================
        // ATTENDANCE
        // =====================================================

        if (
                containsAny(
                        question,
                        "my attendance",
                        "attendance this month",
                        "monthly attendance",
                        "how many days present",
                        "present this month",
                        "mera attendance",
                        "meri attendance"
                )
        ) {

            List<Attendance> attendanceList =
                    attendanceRepository
                            .findByEmployeeIdAndAttendanceDateBetween(
                                    employeeId,
                                    monthStart,
                                    monthEnd
                            );

            int present = 0;
            int late = 0;
            int halfDay = 0;
            int shortHours = 0;

            double totalHours = 0.0;

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

                    case "PRESENT" -> present++;

                    case "LATE" -> late++;

                    case "HALF_DAY" -> halfDay++;

                    case "SHORT_HOURS" -> shortHours++;

                    default -> {
                    }
                }

                if (
                        attendance.getTotalHours() != null
                ) {

                    totalHours +=
                            attendance.getTotalHours();
                }
            }

            return "Your attendance for "
                    + currentMonth.getMonth()
                    + " "
                    + currentMonth.getYear()
                    + ": "
                    + attendanceList.size()
                    + " attendance record(s), "
                    + present
                    + " present, "
                    + late
                    + " late, "
                    + halfDay
                    + " half day, "
                    + shortHours
                    + " short-hours day(s), with "
                    + round(totalHours)
                    + " total recorded working hours.";
        }

        // =====================================================
        // PENDING LEAVES
        // =====================================================

        if (
                containsAny(
                        question,
                        "pending leaves",
                        "pending leave",
                        "how many leaves pending",
                        "my leave requests pending",
                        "mera leave pending",
                        "meri leave pending"
                )
        ) {

            long pendingLeaves =
                    leaveRequestRepository
                            .countByEmployeeIdAndStatus(
                                    employeeId,
                                    "PENDING"
                            );

            return "You currently have "
                    + pendingLeaves
                    + " pending leave request(s).";
        }

        // =====================================================
        // SICK LEAVE BALANCE
        // =====================================================

        if (
                containsAny(
                        question,
                        "sick leave",
                        "sick leaves",
                        "sick balance",
                        "remaining sick",
                        "sick leave balance",
                        "sick kitna bacha",
                        "sick leave kitna bacha",
                        "mera sick leave",
                        "meri sick leave"
                )
        ) {

            LeaveType sickLeaveType =
                    findLeaveTypeByKeyword(
                            "SICK"
                    );

            if (sickLeaveType == null) {

                return "Sick leave type is not configured in the system.";
            }

            return buildSingleLeaveBalanceAnswer(
                    employeeId,
                    sickLeaveType
            );
        }

        // =====================================================
        // CASUAL LEAVE BALANCE
        // =====================================================

        if (
                containsAny(
                        question,
                        "casual leave",
                        "casual leaves",
                        "casual balance",
                        "remaining casual",
                        "casual leave balance",
                        "casual kitna bacha",
                        "casual leave kitna bacha",
                        "mera casual leave",
                        "meri casual leave"
                )
        ) {

            LeaveType casualLeaveType =
                    findLeaveTypeByKeyword(
                            "CASUAL"
                    );

            if (casualLeaveType == null) {

                return "Casual leave type is not configured in the system.";
            }

            return buildSingleLeaveBalanceAnswer(
                    employeeId,
                    casualLeaveType
            );
        }

        // =====================================================
        // EARNED LEAVE BALANCE
        // =====================================================

        if (
                containsAny(
                        question,
                        "earned leave",
                        "earned leaves",
                        "earned balance",
                        "remaining earned",
                        "earned leave balance",
                        "earned kitna bacha",
                        "earned leave kitna bacha",
                        "mera earned leave",
                        "meri earned leave"
                )
        ) {

            LeaveType earnedLeaveType =
                    findLeaveTypeByKeyword(
                            "EARNED"
                    );

            if (earnedLeaveType == null) {

                return "Earned leave type is not configured in the system.";
            }

            return buildSingleLeaveBalanceAnswer(
                    employeeId,
                    earnedLeaveType
            );
        }

        // =====================================================
        // ALL LEAVE BALANCES
        // =====================================================

        if (
                containsAny(
                        question,
                        "leave balance",
                        "remaining leave",
                        "remaining leaves",
                        "how many leaves do i have",
                        "how much leave",
                        "leave kitna bacha",
                        "leave kitni bachi",
                        "mera leave kitna bacha",
                        "meri leave kitni bachi"
                )
        ) {

            return buildAllLeaveBalanceAnswer(
                    employeeId
            );
        }

        // =====================================================
        // SALARY
        // =====================================================

        if (
                containsAny(
                        question,
                        "latest salary",
                        "my salary",
                        "last salary",
                        "recent salary",
                        "net salary",
                        "meri salary",
                        "mera salary"
                )
        ) {

            Salary latestSalary =
                    salaryRepository
                            .findFirstByEmployeeIdOrderBySalaryYearDescSalaryMonthDesc(
                                    employeeId
                            )
                            .orElse(null);

            if (
                    latestSalary == null
            ) {

                return "No salary record is available for your account.";
            }

            return "Your latest salary record is for "
                    + getMonthName(
                    latestSalary.getSalaryMonth()
            )
                    + " "
                    + latestSalary.getSalaryYear()
                    + ". Net salary: Rs. "
                    + latestSalary.getNetSalary()
                    + ".";
        }

        return null;
    }

    // =========================================================
    // ADMIN DIRECT ANSWERS
    // =========================================================

    private String tryAdminDirectAnswer(
            String question
    ) {

        LocalDate today =
                LocalDate.now();

        if (
                containsAny(
                        question,
                        "employees present today",
                        "present today",
                        "how many employees are present"
                )
        ) {

            long presentToday =
                    attendanceRepository
                            .countByAttendanceDate(
                                    today
                            );

            return "There are "
                    + presentToday
                    + " employees present today.";
        }

        if (
                containsAny(
                        question,
                        "total employees",
                        "how many employees",
                        "employee count"
                )
        ) {

            long totalEmployees =
                    employeeRepository.count();

            return "There are "
                    + totalEmployees
                    + " employees in the system.";
        }

        if (
                containsAny(
                        question,
                        "pending leave requests",
                        "pending leaves",
                        "how many leaves are pending"
                )
        ) {

            long pendingLeaves =
                    leaveRequestRepository
                            .countByStatus(
                                    "PENDING"
                            );

            return "There are "
                    + pendingLeaves
                    + " pending leave request(s).";
        }

        if (
                containsAny(
                        question,
                        "this month's payroll",
                        "this month payroll",
                        "monthly payroll",
                        "current payroll"
                )
        ) {

            Double monthlyPayroll =
                    salaryRepository
                            .getMonthlyPayroll(
                                    today.getMonthValue(),
                                    today.getYear()
                            );

            if (
                    monthlyPayroll == null
            ) {

                monthlyPayroll =
                        0.0;
            }

            return "The current month's payroll is Rs. "
                    + String.format(
                    Locale.ENGLISH,
                    "%.2f",
                    monthlyPayroll
            )
                    + ".";
        }

        return null;
    }

    // =========================================================
    // COMMON HOLIDAY DIRECT ANSWER
    // =========================================================

    private String tryHolidayDirectAnswer(
            String question
    ) {

        if (
                !containsAny(
                        question,
                        "next holiday",
                        "upcoming holiday",
                        "when is holiday",
                        "when is the next holiday",
                        "agla holiday",
                        "next chutti"
                )
        ) {

            return null;
        }

        LocalDate today =
                LocalDate.now();

        List<Holiday> holidays =
                holidayRepository
                        .findByHolidayDateGreaterThanEqualOrderByHolidayDateAsc(
                                today
                        );

        Holiday nextHoliday =
                holidays
                        .stream()
                        .findFirst()
                        .orElse(null);

        if (
                nextHoliday == null
        ) {

            return "There is no upcoming holiday configured in the system.";
        }

        return "The next holiday is "
                + nextHoliday.getName()
                + " on "
                + nextHoliday.getHolidayDate()
                + ".";
    }

    // =========================================================
    // EMPLOYEE GEMINI CONTEXT
    // =========================================================

    private String buildEmployeeContext(
            User user
    ) {

        if (
                user.getEmployee() == null
        ) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "User account is not linked with an employee"
            );
        }

        Employee employee =
                user.getEmployee();

        Long employeeId =
                employee.getId();

        LocalDate today =
                LocalDate.now();

        YearMonth currentMonth =
                YearMonth.from(
                        today
                );

        LocalDate monthStart =
                currentMonth.atDay(1);

        LocalDate monthEnd =
                currentMonth.atEndOfMonth();

        List<Attendance> attendanceList =
                attendanceRepository
                        .findByEmployeeIdAndAttendanceDateBetween(
                                employeeId,
                                monthStart,
                                monthEnd
                        );

        int presentDays = 0;
        int lateDays = 0;
        int halfDays = 0;
        int shortHoursDays = 0;

        double totalHours = 0.0;

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

                case "PRESENT" -> presentDays++;

                case "LATE" -> lateDays++;

                case "HALF_DAY" -> halfDays++;

                case "SHORT_HOURS" -> shortHoursDays++;

                default -> {
                }
            }

            if (
                    attendance.getTotalHours() != null
            ) {

                totalHours +=
                        attendance.getTotalHours();
            }
        }

        long pendingLeaves =
                leaveRequestRepository
                        .countByEmployeeIdAndStatus(
                                employeeId,
                                "PENDING"
                        );

        String leaveBalanceText =
                buildLeaveBalanceContext(
                        employeeId
                );

        Salary latestSalary =
                salaryRepository
                        .findFirstByEmployeeIdOrderBySalaryYearDescSalaryMonthDesc(
                                employeeId
                        )
                        .orElse(null);

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

        String latestSalaryText =
                latestSalary == null
                        ? "No salary record available"
                        : "Month="
                          + latestSalary.getSalaryMonth()
                          + ", Year="
                          + latestSalary.getSalaryYear()
                          + ", Net Salary="
                          + latestSalary.getNetSalary();

        String nextHolidayText =
                nextHoliday == null
                        ? "No upcoming holiday configured"
                        : nextHoliday.getHolidayDate()
                          + " - "
                          + nextHoliday.getName();

        return """
                Employee information:
                Employee ID: %d
                Name: %s %s
                Department: %s
                Designation: %s
                
                Current month attendance:
                Attendance records: %d
                Present days: %d
                Late days: %d
                Half days: %d
                Short-hours days: %d
                Total recorded working hours: %.2f
                
                Leave:
                Pending leave requests: %d
                
                Leave balances:
                %s
                
                Latest salary:
                %s
                
                Next holiday:
                %s
                """
                .formatted(
                        employee.getId(),
                        employee.getFirstName(),
                        employee.getLastName(),
                        employee.getDepartment(),
                        employee.getDesignation(),
                        attendanceList.size(),
                        presentDays,
                        lateDays,
                        halfDays,
                        shortHoursDays,
                        totalHours,
                        pendingLeaves,
                        leaveBalanceText,
                        latestSalaryText,
                        nextHolidayText
                );
    }

    // =========================================================
    // ADMIN GEMINI CONTEXT
    // =========================================================

    private String buildAdminContext() {

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

        Double monthlyPayroll =
                salaryRepository
                        .getMonthlyPayroll(
                                today.getMonthValue(),
                                today.getYear()
                        );

        if (
                monthlyPayroll == null
        ) {

            monthlyPayroll =
                    0.0;
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
                        : nextHoliday.getHolidayDate()
                          + " - "
                          + nextHoliday.getName();

        return """
                Organization overview:
                Date: %s
                Total employees: %d
                Present today: %d
                Pending leave requests: %d
                Current month payroll: %.2f
                
                Next holiday:
                %s
                """
                .formatted(
                        today,
                        totalEmployees,
                        presentToday,
                        pendingLeaves,
                        monthlyPayroll,
                        nextHolidayText
                );
    }

    // =========================================================
    // GEMINI PROMPT
    // =========================================================

    private String buildPrompt(
            String question,
            String role,
            String context
    ) {

        return """
                You are an AI assistant inside an Employee Management System.
                
                User role:
                %s
                
                VERIFIED EMPLOYEE MANAGEMENT SYSTEM DATA:
                %s
                
                IMPORTANT RULES:
                - Answer only from the verified system data provided above.
                - Never invent employee data.
                - If information is not present in the verified data, clearly say it is not available.
                - EMPLOYEE users may receive only their own employee data and general company information.
                - ADMIN users may receive organization-level information included in the context.
                - Do not make hiring, firing, promotion, salary-increase, disciplinary, or other employment decisions.
                - Do not provide performance scores or rankings.
                - Keep answers concise and professional.
                - Understand simple Hinglish questions as well as English.
                
                User question:
                %s
                
                Answer:
                """
                .formatted(
                        role,
                        context,
                        question
                );
    }

    // =========================================================
    // LEAVE BALANCE HELPERS
    // =========================================================

    private LeaveType findLeaveTypeByKeyword(
            String keyword
    ) {

        return leaveTypeRepository
                .findAll()
                .stream()
                .filter(leaveType -> {

                    String name =
                            leaveType.getName() == null
                                    ? ""
                                    : leaveType.getName()
                                    .trim()
                                    .toUpperCase();

                    return name.contains(
                            keyword
                    );
                })
                .findFirst()
                .orElse(null);
    }

    private String buildSingleLeaveBalanceAnswer(
            Long employeeId,
            LeaveType leaveType
    ) {

        Integer usedDays =
                leaveRequestRepository
                        .getApprovedLeaveDays(
                                employeeId,
                                leaveType.getId()
                        );

        if (
                usedDays == null
        ) {

            usedDays = 0;
        }

        int leaveLimit =
                getLeaveLimit(
                        leaveType
                );

        int remainingDays =
                Math.max(
                        leaveLimit - usedDays,
                        0
                );

        return "Your "
                + leaveType.getName()
                + " balance is: "
                + remainingDays
                + " day(s) remaining out of "
                + leaveLimit
                + ". You have used "
                + usedDays
                + " day(s).";
    }

    private String buildAllLeaveBalanceAnswer(
            Long employeeId
    ) {

        List<LeaveType> leaveTypes =
                leaveTypeRepository.findAll();

        if (
                leaveTypes.isEmpty()
        ) {

            return "No leave types are configured in the system.";
        }

        StringBuilder answer =
                new StringBuilder(
                        "Your current leave balances are:\n"
                );

        for (
                LeaveType leaveType :
                leaveTypes
        ) {

            Integer usedDays =
                    leaveRequestRepository
                            .getApprovedLeaveDays(
                                    employeeId,
                                    leaveType.getId()
                            );

            if (
                    usedDays == null
            ) {

                usedDays = 0;
            }

            int leaveLimit =
                    getLeaveLimit(
                            leaveType
                    );

            int remainingDays =
                    Math.max(
                            leaveLimit - usedDays,
                            0
                    );

            answer
                    .append("- ")
                    .append(
                            leaveType.getName()
                    )
                    .append(": ")
                    .append(
                            remainingDays
                    )
                    .append(" remaining / ")
                    .append(
                            leaveLimit
                    )
                    .append(" total")
                    .append(
                            "\n"
                    );
        }

        return answer
                .toString()
                .trim();
    }

    private String buildLeaveBalanceContext(
            Long employeeId
    ) {

        List<LeaveType> leaveTypes =
                leaveTypeRepository.findAll();

        if (
                leaveTypes.isEmpty()
        ) {

            return "No leave types configured.";
        }

        StringBuilder context =
                new StringBuilder();

        for (
                LeaveType leaveType :
                leaveTypes
        ) {

            Integer usedDays =
                    leaveRequestRepository
                            .getApprovedLeaveDays(
                                    employeeId,
                                    leaveType.getId()
                            );

            if (
                    usedDays == null
            ) {

                usedDays = 0;
            }

            int leaveLimit =
                    getLeaveLimit(
                            leaveType
                    );

            int remaining =
                    Math.max(
                            leaveLimit - usedDays,
                            0
                    );

            context
                    .append(
                            leaveType.getName()
                    )
                    .append(
                            ": Used="
                    )
                    .append(
                            usedDays
                    )
                    .append(
                            ", Limit="
                    )
                    .append(
                            leaveLimit
                    )
                    .append(
                            ", Remaining="
                    )
                    .append(
                            remaining
                    )
                    .append(
                            "\n"
                    );
        }

        return context
                .toString()
                .trim();
    }

    private int getLeaveLimit(
            LeaveType leaveType
    ) {

        int fallbackLimit =
                leaveType.getMaxDays() != null
                        ? leaveType.getMaxDays()
                        : 0;

        SystemSettings settings =
                systemSettingsRepository
                        .findAll()
                        .stream()
                        .findFirst()
                        .orElse(null);

        if (
                settings == null
        ) {

            return fallbackLimit;
        }

        String leaveName =
                leaveType.getName() == null
                        ? ""
                        : leaveType.getName()
                        .trim()
                        .toUpperCase();

        if (
                leaveName.contains(
                        "CASUAL"
                )
        ) {

            return settings.getCasualLeave() != null
                    ? settings.getCasualLeave()
                    : fallbackLimit;
        }

        if (
                leaveName.contains(
                        "SICK"
                )
        ) {

            return settings.getSickLeave() != null
                    ? settings.getSickLeave()
                    : fallbackLimit;
        }

        if (
                leaveName.contains(
                        "EARNED"
                )
        ) {

            return settings.getEarnedLeave() != null
                    ? settings.getEarnedLeave()
                    : fallbackLimit;
        }

        return fallbackLimit;
    }

    // =========================================================
    // GENERAL HELPERS
    // =========================================================

    private boolean containsAny(
            String value,
            String... keywords
    ) {

        for (
                String keyword :
                keywords
        ) {

            if (
                    value.contains(
                            keyword
                    )
            ) {

                return true;
            }
        }

        return false;
    }

    private String getMonthName(
            Integer month
    ) {

        if (
                month == null
                        ||
                        month < 1
                        ||
                        month > 12
        ) {

            return "--";
        }

        return java.time.Month
                .of(
                        month
                )
                .name();
    }

    private double round(
            double value
    ) {

        return Math.round(
                value * 100.0
        ) / 100.0;
    }

    private AiChatResponse buildResponse(
            String question,
            String answer,
            String role
    ) {

        return AiChatResponse
                .builder()
                .question(
                        question
                )
                .answer(
                        answer
                )
                .role(
                        role
                )
                .timestamp(
                        LocalDateTime.now()
                )
                .build();
    }
}