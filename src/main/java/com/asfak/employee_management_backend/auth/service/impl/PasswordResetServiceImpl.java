package com.asfak.employee_management_backend.auth.service.impl;

import com.asfak.employee_management_backend.auth.dto.ForgotPasswordRequest;
import com.asfak.employee_management_backend.auth.dto.ResetPasswordRequest;
import com.asfak.employee_management_backend.auth.dto.VerifyOtpRequest;

import com.asfak.employee_management_backend.auth.entity.PasswordResetOtp;
import com.asfak.employee_management_backend.auth.repository.PasswordResetOtpRepository;
import com.asfak.employee_management_backend.auth.service.PasswordResetService;

import com.asfak.employee_management_backend.email.service.EmailService;

import com.asfak.employee_management_backend.entity.User;
import com.asfak.employee_management_backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl
        implements PasswordResetService {

    private final UserRepository userRepository;

    private final PasswordResetOtpRepository passwordResetOtpRepository;

    private final EmailService emailService;

    private final PasswordEncoder passwordEncoder;

    private final SecureRandom secureRandom =
            new SecureRandom();

    private static final int OTP_EXPIRY_MINUTES = 5;

    // =========================================================
    // SEND OTP
    // =========================================================

    @Override
    @Transactional
    public void sendOtp(
            ForgotPasswordRequest request
    ) {

        String email =
                normalizeEmail(
                        request.getEmail()
                );

        User user =
                userRepository
                        .findByEmail(email)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "No account found with this email address"
                                )
                        );

        if (
                !user.isEnabled()
        ) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "This account is disabled"
            );
        }

        // Remove old OTPs for this email

        passwordResetOtpRepository
                .deleteByEmail(
                        email
                );

        String otp =
                generateOtp();

        String otpHash =
                passwordEncoder
                        .encode(
                                otp
                        );

        PasswordResetOtp passwordResetOtp =
                PasswordResetOtp
                        .builder()
                        .email(
                                email
                        )
                        .otpHash(
                                otpHash
                        )
                        .expiresAt(
                                LocalDateTime.now()
                                        .plusMinutes(
                                                OTP_EXPIRY_MINUTES
                                        )
                        )
                        .used(
                                false
                        )
                        .createdAt(
                                LocalDateTime.now()
                        )
                        .build();

        passwordResetOtpRepository
                .save(
                        passwordResetOtp
                );

        emailService.sendEmail(
                email,
                "Password Reset OTP",
                "Hello "
                        + getDisplayName(user)
                        + ",\n\n"
                        + "We received a request to reset your Employee Management System password.\n\n"
                        + "Your OTP is: "
                        + otp
                        + "\n\n"
                        + "This OTP is valid for "
                        + OTP_EXPIRY_MINUTES
                        + " minutes.\n\n"
                        + "If you did not request a password reset, you can ignore this email.\n\n"
                        + "Regards,\n"
                        + "Employee Management System"
        );
    }

    // =========================================================
    // VERIFY OTP
    // =========================================================

    @Override
    public void verifyOtp(
            VerifyOtpRequest request
    ) {

        String email =
                normalizeEmail(
                        request.getEmail()
                );

        PasswordResetOtp passwordResetOtp =
                getValidOtpRecord(
                        email
                );

        if (
                !passwordEncoder.matches(
                        request.getOtp(),
                        passwordResetOtp.getOtpHash()
                )
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid OTP"
            );
        }
    }

    // =========================================================
    // RESET PASSWORD
    // =========================================================

    @Override
    @Transactional
    public void resetPassword(
            ResetPasswordRequest request
    ) {

        String email =
                normalizeEmail(
                        request.getEmail()
                );

        User user =
                userRepository
                        .findByEmail(
                                email
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "User not found"
                                )
                        );

        PasswordResetOtp passwordResetOtp =
                getValidOtpRecord(
                        email
                );

        if (
                !passwordEncoder.matches(
                        request.getOtp(),
                        passwordResetOtp.getOtpHash()
                )
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid OTP"
            );
        }

        if (
                passwordEncoder.matches(
                        request.getNewPassword(),
                        user.getPassword()
                )
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "New password cannot be the same as the current password"
            );
        }

        user.setPassword(
                passwordEncoder.encode(
                        request.getNewPassword()
                )
        );

        userRepository.save(
                user
        );

        passwordResetOtp.setUsed(
                true
        );

        passwordResetOtpRepository.save(
                passwordResetOtp
        );

        // Optional but cleaner:
        // remove reset records after successful reset

        passwordResetOtpRepository
                .deleteByEmail(
                        email
                );

        emailService.sendEmail(
                email,
                "Password Reset Successful",
                "Hello "
                        + getDisplayName(user)
                        + ",\n\n"
                        + "Your Employee Management System password has been reset successfully.\n\n"
                        + "If you did not perform this action, please contact your administrator immediately.\n\n"
                        + "Regards,\n"
                        + "Employee Management System"
        );
    }

    // =========================================================
    // VALID OTP RECORD
    // =========================================================

    private PasswordResetOtp getValidOtpRecord(
            String email
    ) {

        PasswordResetOtp passwordResetOtp =
                passwordResetOtpRepository
                        .findFirstByEmailAndUsedFalseOrderByCreatedAtDesc(
                                email
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "OTP not found. Please request a new OTP."
                                )
                        );

        if (
                passwordResetOtp.getExpiresAt()
                        .isBefore(
                                LocalDateTime.now()
                        )
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "OTP has expired. Please request a new OTP."
            );
        }

        return passwordResetOtp;
    }

    // =========================================================
    // OTP GENERATOR
    // =========================================================

    private String generateOtp() {

        int otp =
                secureRandom.nextInt(
                        900000
                ) + 100000;

        return String.valueOf(
                otp
        );
    }

    // =========================================================
    // EMAIL NORMALIZATION
    // =========================================================

    private String normalizeEmail(
            String email
    ) {

        if (
                email == null
        ) {

            return "";
        }

        return email
                .trim()
                .toLowerCase();
    }

    // =========================================================
    // DISPLAY NAME
    // =========================================================

    private String getDisplayName(
            User user
    ) {

        if (
                user.getEmployee() != null
                        &&
                        user.getEmployee().getFirstName() != null
        ) {

            return user
                    .getEmployee()
                    .getFirstName();
        }

        if (
                user.getName() != null
                        &&
                        !user.getName().isBlank()
        ) {

            return user.getName();
        }

        return "User";
    }
}