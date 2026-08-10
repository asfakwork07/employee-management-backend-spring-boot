package com.asfak.employee_management_backend.ai.repository;

import com.asfak.employee_management_backend.ai.entity.PerformanceSummary;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PerformanceSummaryRepository
        extends JpaRepository<PerformanceSummary, Long> {

    Optional<PerformanceSummary>
    findByEmployeeIdAndMonthAndYear(
            Long employeeId,
            Integer month,
            Integer year
    );
}