package com.asfak.employee_management_backend.attendance.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceResponse {

    private Long id;

    private String employeeName;

    private LocalDate attendanceDate;

    private LocalTime checkIn;

    private LocalTime checkOut;

    private Double totalHours;

    private String status;

}