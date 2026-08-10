package com.asfak.employee_management_backend.attendance.entity;

import com.asfak.employee_management_backend.entity.Employee;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "attendance")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Employee
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    // Attendance Date
    @Column(nullable = false)
    private LocalDate attendanceDate;

    // Check In Time
    @Column(nullable = false)
    private LocalTime checkIn;

    // Check Out Time
    private LocalTime checkOut;

    // Total Working Hours
    private Double totalHours;

    // PRESENT / ABSENT / HALF_DAY / WFH
    @Column(nullable = false)
    private String status;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}