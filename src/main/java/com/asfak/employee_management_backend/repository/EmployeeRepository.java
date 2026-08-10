package com.asfak.employee_management_backend.repository;

import com.asfak.employee_management_backend.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // Check duplicate email
    boolean existsByEmail(String email);

    // Check duplicate phone
    boolean existsByPhone(String phone);

    @Query("""
                SELECT e.department, COUNT(e)
                FROM Employee e
                GROUP BY e.department
            """)
    List<Object[]> countEmployeesByDepartment();

}