package com.asfak.employee_management_backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyOtpRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank(message = "OTP is required")
    private String otp;
}