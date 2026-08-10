package com.asfak.employee_management_backend.attendance.service;

import com.asfak.employee_management_backend.attendance.dto.AttendanceResponse;
import com.asfak.employee_management_backend.attendance.dto.CheckInRequest;
import com.asfak.employee_management_backend.attendance.dto.CheckOutRequest;

import java.time.YearMonth;
import java.util.List;

public interface AttendanceService {

    AttendanceResponse checkIn(
            CheckInRequest request,
            String loggedInEmail
    );

    AttendanceResponse checkOut(
            CheckOutRequest request,
            String loggedInEmail
    );

    List<AttendanceResponse> getEmployeeAttendance(
            Long employeeId,
            String loggedInEmail
    );

    List<AttendanceResponse> getAllAttendance();

    List<AttendanceResponse> getTodayAttendance();

    List<AttendanceResponse> getMonthlyAttendance(
            Long employeeId,
            YearMonth month,
            String loggedInEmail
    );
}