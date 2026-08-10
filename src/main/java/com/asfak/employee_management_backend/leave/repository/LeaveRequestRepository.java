package com.asfak.employee_management_backend.leave.repository;

import com.asfak.employee_management_backend.leave.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByEmployeeId(Long employeeId);

    // LeaveRequestRepository.java
    void deleteByEmployeeId(Long employeeId);

    @Query("""
                SELECT COALESCE(SUM(l.totalDays), 0)
                FROM LeaveRequest l
                WHERE l.employee.id = :employeeId
                AND l.leaveType.id = :leaveTypeId
                AND l.status = 'APPROVED'
            """)
    Integer getApprovedLeaveDays(
            @Param("employeeId") Long employeeId,
            @Param("leaveTypeId") Long leaveTypeId
    );

    @Query("""
            SELECT COUNT(l)
            FROM LeaveRequest l
            WHERE l.employee.id = :employeeId
            AND l.status IN ('PENDING','APPROVED')
            AND (
                l.fromDate <= :toDate
                AND l.toDate >= :fromDate
            )
            """)
    Long countOverlappingLeaves(
            @Param("employeeId") Long employeeId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    long countByStatus(String status);

    long countByEmployeeIdAndStatus(
            Long employeeId,
            String status
    );

    @Query("""
                SELECT l.status, COUNT(l)
                FROM LeaveRequest l
                GROUP BY l.status
            """)
    List<Object[]> countLeavesByStatus();

    List<LeaveRequest>
    findByEmployeeIdAndStatusAndToDateGreaterThanEqualAndFromDateLessThanEqual(
            Long employeeId,
            String status,
            LocalDate monthStart,
            LocalDate monthEnd
    );
}