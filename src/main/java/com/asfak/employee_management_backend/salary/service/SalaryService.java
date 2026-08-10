//package com.asfak.employee_management_backend.salary.service;
//
//import com.asfak.employee_management_backend.salary.dto.GenerateSalaryRequest;
//import com.asfak.employee_management_backend.salary.dto.PayslipResponse;
//import com.asfak.employee_management_backend.salary.dto.SalaryResponse;
//
//import java.util.List;
//
//public interface SalaryService {
//
//    SalaryResponse generateSalary(GenerateSalaryRequest request);
//
//    List<SalaryResponse> getAllSalary();
//
//    List<SalaryResponse> getEmployeeSalary(Long employeeId);
//
//    PayslipResponse getPayslip(Long salaryId);
//
//    byte[] generatePayslipPdf(Long salaryId);
//}

package com.asfak.employee_management_backend.salary.service;

import com.asfak.employee_management_backend.salary.dto.GenerateSalaryRequest;
import com.asfak.employee_management_backend.salary.dto.PayslipResponse;
import com.asfak.employee_management_backend.salary.dto.SalaryResponse;

import java.util.List;

public interface SalaryService {

    SalaryResponse generateSalary(
            GenerateSalaryRequest request
    );

    List<SalaryResponse> getAllSalary();

    List<SalaryResponse> getEmployeeSalary(
            Long employeeId,
            String loggedInEmail
    );

    PayslipResponse getPayslip(
            Long salaryId,
            String loggedInEmail
    );

    byte[] generatePayslipPdf(
            Long salaryId,
            String loggedInEmail
    );
}