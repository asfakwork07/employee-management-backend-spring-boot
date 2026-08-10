package com.asfak.employee_management_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class EmployeeAccountResponse {

    private Long userId;

    private Long employeeId;

    private String employeeName;

    private String email;

    private String role;

    private String temporaryPassword;

    private String message;

}