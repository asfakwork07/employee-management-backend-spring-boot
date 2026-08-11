package com.asfak.employee_management_backend.auth.service;

import com.asfak.employee_management_backend.auth.dto.ForgotPasswordRequest;
import com.asfak.employee_management_backend.auth.dto.ResetPasswordRequest;
import com.asfak.employee_management_backend.auth.dto.VerifyOtpRequest;

public interface PasswordResetService {

    void sendOtp(
            ForgotPasswordRequest request
    );

    void verifyOtp(
            VerifyOtpRequest request
    );

    void resetPassword(
            ResetPasswordRequest request
    );
}