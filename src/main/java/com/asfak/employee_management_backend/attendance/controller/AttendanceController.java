package com.asfak.employee_management_backend.attendance.controller;

import com.asfak.employee_management_backend.attendance.dto.AttendanceResponse;
import com.asfak.employee_management_backend.attendance.dto.CheckInRequest;
import com.asfak.employee_management_backend.attendance.dto.CheckOutRequest;
import com.asfak.employee_management_backend.attendance.service.AttendanceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AttendanceController {

    private final AttendanceService attendanceService;


    @PostMapping("/check-in")
    public AttendanceResponse checkIn(
            @Valid @RequestBody CheckInRequest request,
            Authentication authentication
    ) {

        return attendanceService.checkIn(
                request,
                authentication.getName()
        );
    }


    @PutMapping("/check-out")
    public AttendanceResponse checkOut(
            @Valid @RequestBody CheckOutRequest request,
            Authentication authentication
    ) {

        return attendanceService.checkOut(
                request,
                authentication.getName()
        );
    }


    // ADMIN ONLY via SecurityConfig
    @GetMapping
    public List<AttendanceResponse> getAllAttendance() {

        return attendanceService.getAllAttendance();
    }


    @GetMapping("/employee/{employeeId}")
    public List<AttendanceResponse> getEmployeeAttendance(
            @PathVariable Long employeeId,
            Authentication authentication
    ) {

        return attendanceService.getEmployeeAttendance(
                employeeId,
                authentication.getName()
        );
    }


    // ADMIN ONLY via SecurityConfig
    @GetMapping("/today")
    public List<AttendanceResponse> getTodayAttendance() {

        return attendanceService.getTodayAttendance();
    }


    @GetMapping("/monthly/{employeeId}")
    public List<AttendanceResponse> getMonthlyAttendance(
            @PathVariable Long employeeId,
            @RequestParam int year,
            @RequestParam int month,
            Authentication authentication
    ) {

        return attendanceService.getMonthlyAttendance(
                employeeId,
                YearMonth.of(year, month),
                authentication.getName()
        );
    }
}