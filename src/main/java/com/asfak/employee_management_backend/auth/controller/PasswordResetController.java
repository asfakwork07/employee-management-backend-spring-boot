package com.asfak.employee_management_backend.auth.controller;

import com.asfak.employee_management_backend.auth.dto.ForgotPasswordRequest;
import com.asfak.employee_management_backend.auth.dto.ResetPasswordRequest;
import com.asfak.employee_management_backend.auth.dto.VerifyOtpRequest;

import com.asfak.employee_management_backend.auth.service.PasswordResetService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    // =========================================================
    // FORGOT PASSWORD
    // =========================================================

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid
            @RequestBody
            ForgotPasswordRequest request
    ) {

        passwordResetService.sendOtp(
                request
        );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "OTP has been sent to your registered email address."
                )
        );
    }

    // =========================================================
    // VERIFY OTP
    // =========================================================

    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, String>> verifyOtp(
            @Valid
            @RequestBody
            VerifyOtpRequest request
    ) {

        passwordResetService.verifyOtp(
                request
        );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "OTP verified successfully."
                )
        );
    }

    // =========================================================
    // RESET PASSWORD
    // =========================================================

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid
            @RequestBody
            ResetPasswordRequest request
    ) {

        passwordResetService.resetPassword(
                request
        );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Password reset successfully. You can now login with your new password."
                )
        );
    }
}