//package com.asfak.employee_management_backend.dto;
//
//import lombok.Getter;
//import lombok.Setter;
//
//@Setter
//@Getter
//public class LoginResponse {
//
//    private String token;
//    private String message;
//
//    public LoginResponse() {
//    }
//
//    public LoginResponse(String token, String message) {
//        this.token = token;
//        this.message = message;
//    }
//
//}

package com.asfak.employee_management_backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse {

    private String token;
    private String message;
    private String role;
    private String name;
    private String email;
    private Long employeeId;
    private String employeeName;
    private String department;
    private String designation;
    public LoginResponse() {
    }

    public LoginResponse(
            String token,
            String message,
            String role,
            String name,
            String email,
            Long employeeId,
            String employeeName,
            String department,
            String designation
    ) {
        this.token = token;
        this.message = message;
        this.role = role;
        this.name = name;
        this.email = email;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.department = department;
        this.designation = designation;
    }
}