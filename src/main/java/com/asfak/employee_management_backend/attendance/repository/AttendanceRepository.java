package com.asfak.employee_management_backend.attendance.repository;

import com.asfak.employee_management_backend.attendance.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    // AttendanceRepository.java
    void deleteByEmployeeId(Long employeeId);
    Optional<Attendance> findByEmployeeIdAndAttendanceDate(
            Long employeeId,
            LocalDate attendanceDate
    );

    List<Attendance> findByEmployeeIdOrderByAttendanceDateDesc(
            Long employeeId
    );

    List<Attendance> findByAttendanceDate(LocalDate attendanceDate);

    List<Attendance> findByEmployeeIdAndAttendanceDateBetween(
            Long employeeId,
            LocalDate start,
            LocalDate end
    );


    long countByAttendanceDate(LocalDate attendanceDate);
}