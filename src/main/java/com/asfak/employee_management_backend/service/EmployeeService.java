package com.asfak.employee_management_backend.service;

import com.asfak.employee_management_backend.dto.EmployeeResponse;
import com.asfak.employee_management_backend.entity.Employee;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface EmployeeService {

    Employee saveEmployee(Employee employee);
    EmployeeResponse getMyProfile(String loggedInEmail);
//    List<Employee> getAllEmployees();
    List<EmployeeResponse> getAllEmployees();
    Employee getEmployeeById(Long id);

    Employee updateEmployee(Long id, Employee employee);

    void deleteEmployee(Long id);

    String importEmployees(MultipartFile file);
}