package com.asfak.employee_management_backend.salary.mapper;

import com.asfak.employee_management_backend.salary.dto.PayslipResponse;
import com.asfak.employee_management_backend.salary.dto.SalaryResponse;
import com.asfak.employee_management_backend.salary.entity.Salary;
import org.springframework.stereotype.Component;

@Component
public class SalaryMapper {

    public SalaryResponse toSalaryResponse(Salary salary) {

        return SalaryResponse.builder()
                .id(salary.getId())
                .employeeName(
                        salary.getEmployee().getFirstName()
                                + " "
                                + salary.getEmployee().getLastName()
                )
                .salaryMonth(salary.getSalaryMonth())
                .salaryYear(salary.getSalaryYear())
                .basicSalary(salary.getBasicSalary())
                .hra(salary.getHra())
                .allowance(salary.getAllowance())
                .pf(salary.getPf())
                .professionalTax(salary.getProfessionalTax())
                .incomeTax(salary.getIncomeTax())
                .grossSalary(salary.getGrossSalary())
                .netSalary(salary.getNetSalary())
                .generatedDate(salary.getGeneratedDate())
                .build();
    }

    public PayslipResponse toPayslipResponse(Salary salary) {

        return PayslipResponse.builder()
                .employeeName(
                        salary.getEmployee().getFirstName()
                                + " "
                                + salary.getEmployee().getLastName()
                )
                .department(salary.getEmployee().getDepartment())
                .designation(salary.getEmployee().getDesignation())
                .salaryMonth(salary.getSalaryMonth())
                .salaryYear(salary.getSalaryYear())
                .basicSalary(salary.getBasicSalary())
                .hra(salary.getHra())
                .allowance(salary.getAllowance())
                .pf(salary.getPf())
                .professionalTax(salary.getProfessionalTax())
                .incomeTax(salary.getIncomeTax())
                .grossSalary(salary.getGrossSalary())
                .netSalary(salary.getNetSalary())
                .build();
    }
}