package com.asfak.employee_management_backend.salary.repository;

import com.asfak.employee_management_backend.salary.entity.Salary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SalaryRepository extends JpaRepository<Salary, Long> {
    // SalaryRepository.java
    void deleteByEmployeeId(Long employeeId);

    Optional<Salary> findByEmployeeIdAndSalaryMonthAndSalaryYear(
            Long employeeId,
            Integer salaryMonth,
            Integer salaryYear
    );

    List<Salary> findByEmployeeIdOrderBySalaryYearDescSalaryMonthDesc(
            Long employeeId
    );

    @Query("""
            SELECT COALESCE(SUM(s.netSalary),0)
            FROM Salary s
            WHERE s.salaryMonth=:month
            AND s.salaryYear=:year
            """)
    Double getMonthlyPayroll(
            @Param("month") Integer month,
            @Param("year") Integer year
    );

    Optional<Salary>
    findFirstByEmployeeIdOrderBySalaryYearDescSalaryMonthDesc(
            Long employeeId
    );
}