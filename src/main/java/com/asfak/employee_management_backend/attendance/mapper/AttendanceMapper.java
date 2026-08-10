package com.asfak.employee_management_backend.attendance.mapper;

import com.asfak.employee_management_backend.attendance.dto.AttendanceResponse;
import com.asfak.employee_management_backend.attendance.entity.Attendance;
import org.springframework.stereotype.Component;

@Component
public class AttendanceMapper {

    public AttendanceResponse toAttendanceResponse(Attendance attendance) {

        return AttendanceResponse.builder()
                .id(attendance.getId())
                .employeeName(
                        attendance.getEmployee().getFirstName()
                                + " "
                                + attendance.getEmployee().getLastName()
                )
                .attendanceDate(attendance.getAttendanceDate())
                .checkIn(attendance.getCheckIn())
                .checkOut(attendance.getCheckOut())
                .totalHours(attendance.getTotalHours())
                .status(attendance.getStatus())
                .build();
    }
}