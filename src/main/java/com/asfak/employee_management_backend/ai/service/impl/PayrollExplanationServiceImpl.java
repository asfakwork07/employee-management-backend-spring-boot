package com.asfak.employee_management_backend.ai.service.impl;

import com.asfak.employee_management_backend.ai.dto.PayrollExplanationResponse;
import com.asfak.employee_management_backend.ai.service.AiService;
import com.asfak.employee_management_backend.ai.service.PayrollExplanationService;

import com.asfak.employee_management_backend.entity.User;
import com.asfak.employee_management_backend.repository.UserRepository;

import com.asfak.employee_management_backend.salary.entity.Salary;
import com.asfak.employee_management_backend.salary.repository.SalaryRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PayrollExplanationServiceImpl
        implements PayrollExplanationService {

    private final SalaryRepository salaryRepository;

    private final UserRepository userRepository;

    private final AiService aiService;

    @Override
    public PayrollExplanationResponse explainSalary(
            Long salaryId,
            String loggedInEmail
    ) {

        Salary salary =
                salaryRepository
                        .findById(salaryId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Salary record not found"
                                )
                        );

        validateSalaryAccess(
                salary,
                loggedInEmail
        );

        BigDecimal totalDeductions =
                nullSafe(salary.getPf())
                        .add(
                                nullSafe(
                                        salary.getProfessionalTax()
                                )
                        )
                        .add(
                                nullSafe(
                                        salary.getIncomeTax()
                                )
                        );

        String employeeName =
                buildEmployeeName(
                        salary
                );

        String monthName =
                getMonthName(
                        salary.getSalaryMonth()
                );

        String prompt =
                buildPrompt(
                        salary,
                        employeeName,
                        monthName,
                        totalDeductions
                );

        String explanation =
                aiService.generateText(
                        prompt
                );

        return PayrollExplanationResponse
                .builder()
                .salaryId(
                        salary.getId()
                )
                .employeeName(
                        employeeName
                )
                .salaryMonth(
                        salary.getSalaryMonth()
                )
                .salaryYear(
                        salary.getSalaryYear()
                )
                .basicSalary(
                        salary.getBasicSalary()
                )
                .hra(
                        salary.getHra()
                )
                .allowance(
                        salary.getAllowance()
                )
                .grossSalary(
                        salary.getGrossSalary()
                )
                .pf(
                        salary.getPf()
                )
                .professionalTax(
                        salary.getProfessionalTax()
                )
                .incomeTax(
                        salary.getIncomeTax()
                )
                .totalDeductions(
                        totalDeductions
                )
                .netSalary(
                        salary.getNetSalary()
                )
                .explanation(
                        explanation
                )
                .build();
    }

    private void validateSalaryAccess(
            Salary salary,
            String loggedInEmail
    ) {

        User user =
                userRepository
                        .findByEmail(
                                loggedInEmail
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "Logged-in user not found"
                                )
                        );

        if (
                "ADMIN".equalsIgnoreCase(
                        user.getRole()
                )
        ) {
            return;
        }

        if (
                user.getEmployee() == null
        ) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "User account is not linked with an employee"
            );
        }

        Long loggedInEmployeeId =
                user.getEmployee()
                        .getId();

        Long salaryEmployeeId =
                salary.getEmployee()
                        .getId();

        if (
                !loggedInEmployeeId.equals(
                        salaryEmployeeId
                )
        ) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You can only explain your own salary"
            );
        }
    }

    private String buildPrompt(
            Salary salary,
            String employeeName,
            String monthName,
            BigDecimal totalDeductions
    ) {

        return """
                You are an AI payroll assistant inside an Employee Management System.

                Explain the following salary record in simple,
                professional and employee-friendly language.

                IMPORTANT RULES:
                - Use only the salary information provided below.
                - Do not invent any salary component.
                - Do not change or recalculate the provided values.
                - Do not provide tax, legal, financial or investment advice.
                - Do not judge employee performance.
                - Do not recommend salary increases, promotions,
                  termination or any employment decision.
                - Clearly explain gross salary, deductions and net salary.
                - Keep the explanation concise.
                - Use 4 to 6 sentences.
                - Format currency values using Rs.

                Employee:
                %s

                Salary Period:
                %s %d

                Earnings:

                Basic Salary: Rs. %s
                HRA: Rs. %s
                Allowance: Rs. %s
                Gross Salary: Rs. %s

                Deductions:

                PF: Rs. %s
                Professional Tax: Rs. %s
                Income Tax: Rs. %s
                Total Deductions: Rs. %s

                Final Net Salary:
                Rs. %s

                Explain this salary breakdown now.
                """
                .formatted(
                        employeeName,
                        monthName,
                        salary.getSalaryYear(),
                        money(salary.getBasicSalary()),
                        money(salary.getHra()),
                        money(salary.getAllowance()),
                        money(salary.getGrossSalary()),
                        money(salary.getPf()),
                        money(salary.getProfessionalTax()),
                        money(salary.getIncomeTax()),
                        money(totalDeductions),
                        money(salary.getNetSalary())
                );
    }

    private String buildEmployeeName(
            Salary salary
    ) {

        String firstName =
                salary.getEmployee()
                        .getFirstName();

        String lastName =
                salary.getEmployee()
                        .getLastName();

        return (
                nullSafeString(firstName)
                        + " "
                        + nullSafeString(lastName)
        ).trim();
    }

    private String getMonthName(
            Integer month
    ) {

        if (
                month == null ||
                        month < 1 ||
                        month > 12
        ) {
            return "Unknown";
        }

        return Month
                .of(month)
                .getDisplayName(
                        TextStyle.FULL,
                        Locale.ENGLISH
                );
    }

    private BigDecimal nullSafe(
            BigDecimal value
    ) {

        return value != null
                ? value
                : BigDecimal.ZERO;
    }

    private String nullSafeString(
            String value
    ) {

        return value != null
                ? value
                : "";
    }

    private String money(
            BigDecimal value
    ) {

        return nullSafe(value)
                .setScale(2)
                .toPlainString();
    }
}