package com.asfak.employee_management_backend.attendance.service.impl;

import com.asfak.employee_management_backend.attendance.dto.AttendanceResponse;
import com.asfak.employee_management_backend.attendance.dto.CheckInRequest;
import com.asfak.employee_management_backend.attendance.dto.CheckOutRequest;
import com.asfak.employee_management_backend.attendance.entity.Attendance;
import com.asfak.employee_management_backend.attendance.mapper.AttendanceMapper;
import com.asfak.employee_management_backend.attendance.repository.AttendanceRepository;
import com.asfak.employee_management_backend.attendance.service.AttendanceService;
import com.asfak.employee_management_backend.entity.Employee;
import com.asfak.employee_management_backend.entity.User;
import com.asfak.employee_management_backend.holiday.repository.HolidayRepository;
import com.asfak.employee_management_backend.repository.EmployeeRepository;
import com.asfak.employee_management_backend.repository.UserRepository;
import com.asfak.employee_management_backend.settings.entity.SystemSettings;
import com.asfak.employee_management_backend.settings.repository.SystemSettingsRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;

    private final EmployeeRepository employeeRepository;

    private final AttendanceMapper attendanceMapper;

    private final UserRepository userRepository;

    private final SystemSettingsRepository systemSettingsRepository;

    private final HolidayRepository holidayRepository;

    @Override
    public AttendanceResponse checkIn(
            CheckInRequest request,
            String loggedInEmail
    ) {

        validateAttendanceOwnership(
                request.getEmployeeId(),
                loggedInEmail
        );

        Employee employee =
                employeeRepository
                        .findById(request.getEmployeeId())
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Employee not found"
                                )
                        );

        LocalDate today =
                LocalDate.now();

        if (holidayRepository.existsByHolidayDate(today)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Today is a company holiday. Attendance check-in is not required."
            );
        }

        attendanceRepository
                .findByEmployeeIdAndAttendanceDate(
                        employee.getId(),
                        today
                )
                .ifPresent(attendance -> {

                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Employee already checked in today."
                    );

                });

        LocalTime currentTime =
                LocalTime.now();

        String attendanceStatus =
                calculateAttendanceStatus(
                        currentTime
                );

        Attendance attendance =
                Attendance.builder()
                        .employee(employee)
                        .attendanceDate(today)
                        .checkIn(currentTime)
                        .status(attendanceStatus)
                        .build();

        Attendance saved =
                attendanceRepository.save(attendance);

        return attendanceMapper
                .toAttendanceResponse(saved);
    }

//    @Override
//    public AttendanceResponse checkOut(
//            CheckOutRequest request,
//            String loggedInEmail
//    ) {
//
//        validateAttendanceOwnership(
//                request.getEmployeeId(),
//                loggedInEmail
//        );
//
//        Attendance attendance =
//                attendanceRepository
//                        .findByEmployeeIdAndAttendanceDate(
//                                request.getEmployeeId(),
//                                LocalDate.now()
//                        )
//                        .orElseThrow(() ->
//                                new ResponseStatusException(
//                                        HttpStatus.NOT_FOUND,
//                                        "No check-in found for today."
//                                )
//                        );
//
//        if (
//                attendance.getCheckOut() != null
//        ) {
//
//            throw new ResponseStatusException(
//                    HttpStatus.BAD_REQUEST,
//                    "Employee already checked out."
//            );
//        }
//
//        LocalTime checkOutTime =
//                LocalTime.now();
//
//        attendance.setCheckOut(
//                checkOutTime
//        );
//
//        double hours =
//                ChronoUnit.MINUTES.between(
//                        attendance.getCheckIn(),
//                        checkOutTime
//                ) / 60.0;
//
//        attendance.setTotalHours(
//                hours
//        );
//
//        Attendance saved =
//                attendanceRepository.save(attendance);
//
//        return attendanceMapper
//                .toAttendanceResponse(saved);
//    }

    @Override
    public AttendanceResponse checkOut(
            CheckOutRequest request,
            String loggedInEmail
    ) {

        validateAttendanceOwnership(
                request.getEmployeeId(),
                loggedInEmail
        );

        Attendance attendance =
                attendanceRepository
                        .findByEmployeeIdAndAttendanceDate(
                                request.getEmployeeId(),
                                LocalDate.now()
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "No check-in found for today."
                                )
                        );

        if (attendance.getCheckOut() != null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Employee already checked out."
            );
        }

        LocalTime checkOutTime =
                LocalTime.now();

        attendance.setCheckOut(
                checkOutTime
        );

        double hours =
                ChronoUnit.MINUTES.between(
                        attendance.getCheckIn(),
                        checkOutTime
                ) / 60.0;

        attendance.setTotalHours(
                hours
        );

        int requiredWorkingHours =
                getRequiredWorkingHours();

        if (hours < 4) {

            attendance.setStatus(
                    "SHORT_HOURS"
            );

        } else if (
                hours < requiredWorkingHours
        ) {

            attendance.setStatus(
                    "HALF_DAY"
            );

        }

        Attendance saved =
                attendanceRepository.save(
                        attendance
                );

        return attendanceMapper
                .toAttendanceResponse(
                        saved
                );
    }

    @Override
    public List<AttendanceResponse> getEmployeeAttendance(
            Long employeeId,
            String loggedInEmail
    ) {

        validateAttendanceOwnership(
                employeeId,
                loggedInEmail
        );

        return attendanceRepository
                .findByEmployeeIdOrderByAttendanceDateDesc(
                        employeeId
                )
                .stream()
                .map(
                        attendanceMapper::toAttendanceResponse
                )
                .toList();
    }

    @Override
    public List<AttendanceResponse> getAllAttendance() {

        return attendanceRepository
                .findAll()
                .stream()
                .map(
                        attendanceMapper::toAttendanceResponse
                )
                .toList();
    }

    @Override
    public List<AttendanceResponse> getTodayAttendance() {

        return attendanceRepository
                .findByAttendanceDate(
                        LocalDate.now()
                )
                .stream()
                .map(
                        attendanceMapper::toAttendanceResponse
                )
                .toList();
    }

    @Override
    public List<AttendanceResponse> getMonthlyAttendance(
            Long employeeId,
            YearMonth month,
            String loggedInEmail
    ) {

        validateAttendanceOwnership(
                employeeId,
                loggedInEmail
        );

        LocalDate start =
                month.atDay(1);

        LocalDate end =
                month.atEndOfMonth();

        return attendanceRepository
                .findByEmployeeIdAndAttendanceDateBetween(
                        employeeId,
                        start,
                        end
                )
                .stream()
                .map(
                        attendanceMapper::toAttendanceResponse
                )
                .toList();
    }

    private String calculateAttendanceStatus(
            LocalTime checkInTime
    ) {

        SystemSettings settings =
                systemSettingsRepository
                        .findAll()
                        .stream()
                        .findFirst()
                        .orElse(null);

        LocalTime officeStartTime =
                LocalTime.of(
                        9,
                        30
                );

        int gracePeriod =
                15;

        if (
                settings != null
        ) {

            if (
                    settings.getOfficeStartTime() != null &&
                            !settings.getOfficeStartTime().isBlank()
            ) {

                officeStartTime =
                        LocalTime.parse(
                                settings.getOfficeStartTime()
                        );
            }

            if (
                    settings.getGracePeriod() != null
            ) {

                gracePeriod =
                        settings.getGracePeriod();
            }
        }

        LocalTime allowedTime =
                officeStartTime.plusMinutes(
                        gracePeriod
                );

        if (
                checkInTime.isAfter(
                        allowedTime
                )
        ) {

            return "LATE";
        }

        return "PRESENT";
    }

    private User getLoggedInUser(
            String email
    ) {

        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Logged-in user not found"
                        )
                );
    }

    private void validateAttendanceOwnership(
            Long employeeId,
            String loggedInEmail
    ) {

        User user =
                getLoggedInUser(
                        loggedInEmail
                );

        if (
                "ADMIN".equalsIgnoreCase(
                        user.getRole()
                )
        ) {
            return;
        }

        if (
                user.getEmployee() == null
        ) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "User account is not linked with an employee"
            );
        }

        Long loggedInEmployeeId =
                user.getEmployee().getId();

        if (
                !loggedInEmployeeId.equals(
                        employeeId
                )
        ) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You can only access your own attendance"
            );
        }
    }

    private int getRequiredWorkingHours() {

        SystemSettings settings =
                systemSettingsRepository
                        .findAll()
                        .stream()
                        .findFirst()
                        .orElse(null);

        if (
                settings != null &&
                        settings.getWorkingHours() != null &&
                        settings.getWorkingHours() > 0
        ) {

            return settings.getWorkingHours();
        }

        return 9;
    }
}