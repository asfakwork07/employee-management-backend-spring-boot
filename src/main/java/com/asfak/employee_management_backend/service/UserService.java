package com.asfak.employee_management_backend.service;

import com.asfak.employee_management_backend.dto.EmployeeAccountResponse;

public interface UserService {

    EmployeeAccountResponse createEmployeeAccount(
            Long employeeId
    );


    EmployeeAccountResponse resetEmployeePassword(
            Long employeeId
    );
    EmployeeAccountResponse disableEmployeeAccount(
            Long employeeId
    );

    EmployeeAccountResponse enableEmployeeAccount(
            Long employeeId
    );
}