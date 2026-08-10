package com.asfak.employee_management_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeResponse {

    private Long id;

    private String firstName;

    private String lastName;

    private String email;

    private String phone;

    private String department;

    private String designation;

    private BigDecimal salary;

    private LocalDate joiningDate;

    private String status;

    private boolean loginEnabled;


    private Boolean accountEnabled;
}