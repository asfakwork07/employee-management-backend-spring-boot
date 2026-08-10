package com.asfak.employee_management_backend.dashboard.service.impl;

import com.asfak.employee_management_backend.attendance.entity.Attendance;
import com.asfak.employee_management_backend.attendance.repository.AttendanceRepository;
import com.asfak.employee_management_backend.dashboard.dto.DashboardResponse;
import com.asfak.employee_management_backend.dashboard.dto.MonthlyPayrollResponse;
import com.asfak.employee_management_backend.dashboard.service.DashboardService;
import com.asfak.employee_management_backend.entity.User;
import com.asfak.employee_management_backend.holiday.repository.HolidayRepository;
import com.asfak.employee_management_backend.leave.repository.LeaveRequestRepository;
import com.asfak.employee_management_backend.repository.EmployeeRepository;
import com.asfak.employee_management_backend.repository.UserRepository;
import com.asfak.employee_management_backend.salary.entity.Salary;
import com.asfak.employee_management_backend.salary.repository.SalaryRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final EmployeeRepository employeeRepository;

    private final AttendanceRepository attendanceRepository;

    private final LeaveRequestRepository leaveRequestRepository;

    private final SalaryRepository salaryRepository;

    private final UserRepository userRepository;

    private final HolidayRepository holidayRepository;

    @Override
    public DashboardResponse getDashboard(
            String loggedInEmail
    ) {

        User user =
                userRepository
                        .findByEmail(loggedInEmail)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "Logged-in user not found"
                                )
                        );

        if (
                "ADMIN".equalsIgnoreCase(
                        user.getRole()
                )
        ) {

            return getAdminDashboard();
        }

        return getEmployeeDashboard(user);
    }

    private DashboardResponse getAdminDashboard() {

        LocalDate today =
                LocalDate.now();

        boolean holidayToday =
                holidayRepository
                        .existsByHolidayDate(today);

        long totalEmployees =
                employeeRepository.count();

        long presentToday =
                attendanceRepository
                        .countByAttendanceDate(today);

        long absentToday;

        if (holidayToday) {

            absentToday = 0;

        } else {

            absentToday =
                    Math.max(
                            totalEmployees - presentToday,
                            0
                    );
        }

        long pendingLeaves =
                leaveRequestRepository
                        .countByStatus("PENDING");

        Double monthlyPayroll =
                salaryRepository
                        .getMonthlyPayroll(
                                today.getMonthValue(),
                                today.getYear()
                        );

        if (monthlyPayroll == null) {

            monthlyPayroll = 0.0;
        }

        Map<String, Long> employeesByDepartment =
                getEmployeesByDepartment();

        Map<String, Long> leaveStatusStats =
                getLeaveStatusStats();

        List<MonthlyPayrollResponse> monthlyPayrollStats =
                getMonthlyPayrollStats(
                        today.getYear()
                );

        return DashboardResponse
                .builder()
                .role("ADMIN")
                .totalEmployees(
                        totalEmployees
                )
                .presentToday(
                        presentToday
                )
                .absentToday(
                        absentToday
                )
                .pendingLeaves(
                        pendingLeaves
                )
                .monthlyPayroll(
                        monthlyPayroll
                )
                .employeesByDepartment(
                        employeesByDepartment
                )
                .leaveStatusStats(
                        leaveStatusStats
                )
                .monthlyPayrollStats(
                        monthlyPayrollStats
                )
                .build();
    }

    private DashboardResponse getEmployeeDashboard(
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

        Long employeeId =
                user.getEmployee()
                        .getId();

        String employeeName =
                user.getEmployee()
                        .getFirstName()
                        + " "
                        + user.getEmployee()
                        .getLastName();

        LocalDate today =
                LocalDate.now();

        boolean holidayToday =
                holidayRepository
                        .existsByHolidayDate(today);

        Attendance todayAttendance =
                attendanceRepository
                        .findByEmployeeIdAndAttendanceDate(
                                employeeId,
                                today
                        )
                        .orElse(null);

        long pendingLeaves =
                leaveRequestRepository
                        .countByEmployeeIdAndStatus(
                                employeeId,
                                "PENDING"
                        );

        Salary latestSalary =
                salaryRepository
                        .findFirstByEmployeeIdOrderBySalaryYearDescSalaryMonthDesc(
                                employeeId
                        )
                        .orElse(null);

        String attendanceStatus;

        if (holidayToday) {

            attendanceStatus =
                    "HOLIDAY";

        } else if (
                todayAttendance != null
        ) {

            attendanceStatus =
                    todayAttendance
                            .getStatus();

        } else {

            attendanceStatus =
                    "ABSENT";
        }

        String checkIn = null;

        String checkOut = null;

        double workingHours = 0.0;

        if (
                todayAttendance != null
        ) {

            if (
                    todayAttendance.getCheckIn() != null
            ) {

                checkIn =
                        todayAttendance
                                .getCheckIn()
                                .toString();
            }

            if (
                    todayAttendance.getCheckOut() != null
            ) {

                checkOut =
                        todayAttendance
                                .getCheckOut()
                                .toString();
            }

            if (
                    todayAttendance.getTotalHours() != null
            ) {

                workingHours =
                        todayAttendance
                                .getTotalHours();
            }
        }

        return DashboardResponse
                .builder()
                .role("EMPLOYEE")
                .employeeId(
                        employeeId
                )
                .employeeName(
                        employeeName
                )
                .attendanceStatus(
                        attendanceStatus
                )
                .checkIn(
                        checkIn
                )
                .checkOut(
                        checkOut
                )
                .workingHours(
                        workingHours
                )
                .myPendingLeaves(
                        pendingLeaves
                )
                .latestNetSalary(
                        latestSalary != null &&
                                latestSalary.getNetSalary() != null
                                ? latestSalary
                                .getNetSalary()
                                .doubleValue()
                                : 0.0
                )
                .build();
    }

    private Map<String, Long> getEmployeesByDepartment() {

        Map<String, Long> result =
                new LinkedHashMap<>();

        List<Object[]> rows =
                employeeRepository
                        .countEmployeesByDepartment();

        for (
                Object[] row : rows
        ) {

            String department =
                    String.valueOf(
                            row[0]
                    );

            Long count =
                    (Long) row[1];

            result.put(
                    department,
                    count
            );
        }

        return result;
    }

    private Map<String, Long> getLeaveStatusStats() {

        Map<String, Long> result =
                new LinkedHashMap<>();

        result.put(
                "PENDING",
                0L
        );

        result.put(
                "APPROVED",
                0L
        );

        result.put(
                "REJECTED",
                0L
        );

        List<Object[]> rows =
                leaveRequestRepository
                        .countLeavesByStatus();

        for (
                Object[] row : rows
        ) {

            String status =
                    String.valueOf(
                            row[0]
                    );

            Long count =
                    (Long) row[1];

            result.put(
                    status,
                    count
            );
        }

        return result;
    }

    private List<MonthlyPayrollResponse>
    getMonthlyPayrollStats(
            int year
    ) {

        List<MonthlyPayrollResponse> result =
                new ArrayList<>();

        for (
                int month = 1;
                month <= 12;
                month++
        ) {

            Double amount =
                    salaryRepository
                            .getMonthlyPayroll(
                                    month,
                                    year
                            );

            if (
                    amount == null
            ) {

                amount = 0.0;
            }

            result.add(
                    MonthlyPayrollResponse
                            .builder()
                            .month(
                                    month
                            )
                            .monthName(
                                    Month
                                            .of(month)
                                            .name()
                            )
                            .amount(
                                    amount
                            )
                            .build()
            );
        }

        return result;
    }
}