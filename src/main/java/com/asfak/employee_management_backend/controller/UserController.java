package com.asfak.employee_management_backend.controller;

import com.asfak.employee_management_backend.dto.EmployeeAccountResponse;
import com.asfak.employee_management_backend.service.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    @PostMapping(
            "/employee/{employeeId}/account"
    )
    public ResponseEntity<EmployeeAccountResponse>
    createEmployeeAccount(
            @PathVariable Long employeeId
    ) {

        EmployeeAccountResponse response =
                userService
                        .createEmployeeAccount(
                                employeeId
                        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/employee/{employeeId}/reset-password")
    public EmployeeAccountResponse resetEmployeePassword(
            @PathVariable Long employeeId
    ) {

        return userService.resetEmployeePassword(
                employeeId
        );
    }

    @PutMapping("/employee/{employeeId}/disable")
    public EmployeeAccountResponse disableEmployeeAccount(
            @PathVariable Long employeeId
    ) {

        return userService.disableEmployeeAccount(
                employeeId
        );
    }

    @PutMapping("/employee/{employeeId}/enable")
    public EmployeeAccountResponse enableEmployeeAccount(
            @PathVariable Long employeeId
    ) {

        return userService.enableEmployeeAccount(
                employeeId
        );
    }
}