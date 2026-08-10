//package com.asfak.employee_management_backend.salary.controller;
//
//import com.asfak.employee_management_backend.salary.dto.GenerateSalaryRequest;
//import com.asfak.employee_management_backend.salary.dto.PayslipResponse;
//import com.asfak.employee_management_backend.salary.dto.SalaryResponse;
//import com.asfak.employee_management_backend.salary.service.SalaryService;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.http.ResponseEntity;
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/salary")
//@RequiredArgsConstructor
//@CrossOrigin(origins = "*")
//public class SalaryController {
//
//    private final SalaryService salaryService;
//
//    @PostMapping("/generate")
//    public SalaryResponse generateSalary(
//            @Valid @RequestBody GenerateSalaryRequest request
//    ) {
//        return salaryService.generateSalary(request);
//    }
//    @GetMapping
//    public List<SalaryResponse> getAllSalary() {
//
//        return salaryService.getAllSalary();
//
//    }
//    @GetMapping("/employee/{employeeId}")
//    public List<SalaryResponse> getEmployeeSalary(
//            @PathVariable Long employeeId
//    ) {
//
//        return salaryService.getEmployeeSalary(employeeId);
//
//    }
//    @GetMapping("/payslip/{salaryId}")
//    public PayslipResponse getPayslip(
//            @PathVariable Long salaryId
//    ) {
//
//        return salaryService.getPayslip(salaryId);
//
//    }
//
//    @GetMapping(
//            value = "/payslip/{salaryId}/pdf",
//            produces = "application/pdf"
//    )
//    public ResponseEntity<byte[]> downloadPayslip(
//            @PathVariable Long salaryId
//    ) {
//
//        byte[] pdf =
//                salaryService.generatePayslipPdf(salaryId);
//
//        return ResponseEntity.ok()
//                .header(
//                        "Content-Disposition",
//                        "attachment; filename=payslip-"
//                                + salaryId
//                                + ".pdf"
//                )
//                .body(pdf);
//    }
//
//}

package com.asfak.employee_management_backend.salary.controller;

import com.asfak.employee_management_backend.salary.dto.GenerateSalaryRequest;
import com.asfak.employee_management_backend.salary.dto.PayslipResponse;
import com.asfak.employee_management_backend.salary.dto.SalaryResponse;
import com.asfak.employee_management_backend.salary.service.SalaryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/salary")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SalaryController {

    private final SalaryService salaryService;


    // =========================================================
    // GENERATE SALARY
    // ADMIN ONLY - SecurityConfig will protect this
    // =========================================================

    @PostMapping("/generate")
    public SalaryResponse generateSalary(
            @Valid @RequestBody GenerateSalaryRequest request
    ) {

        return salaryService.generateSalary(request);
    }


    // =========================================================
    // GET ALL SALARY
    // ADMIN ONLY
    // =========================================================

    @GetMapping
    public List<SalaryResponse> getAllSalary() {

        return salaryService.getAllSalary();
    }


    // =========================================================
    // EMPLOYEE SALARY HISTORY
    // ADMIN = any employee
    // EMPLOYEE = own salary only
    // =========================================================

    @GetMapping("/employee/{employeeId}")
    public List<SalaryResponse> getEmployeeSalary(
            @PathVariable Long employeeId,
            Authentication authentication
    ) {

        String email =
                authentication.getName();

        return salaryService.getEmployeeSalary(
                employeeId,
                email
        );
    }


    // =========================================================
    // VIEW PAYSLIP
    // =========================================================

    @GetMapping("/payslip/{salaryId}")
    public PayslipResponse getPayslip(
            @PathVariable Long salaryId,
            Authentication authentication
    ) {

        String email =
                authentication.getName();

        return salaryService.getPayslip(
                salaryId,
                email
        );
    }


    // =========================================================
    // DOWNLOAD PAYSLIP PDF
    // =========================================================

    @GetMapping(
            value = "/payslip/{salaryId}/pdf",
            produces = "application/pdf"
    )
    public ResponseEntity<byte[]> downloadPayslip(
            @PathVariable Long salaryId,
            Authentication authentication
    ) {

        String email =
                authentication.getName();

        byte[] pdf =
                salaryService.generatePayslipPdf(
                        salaryId,
                        email
                );


        return ResponseEntity
                .ok()
                .header(
                        "Content-Disposition",
                        "attachment; filename=payslip-"
                                + salaryId
                                + ".pdf"
                )
                .body(pdf);
    }
}
