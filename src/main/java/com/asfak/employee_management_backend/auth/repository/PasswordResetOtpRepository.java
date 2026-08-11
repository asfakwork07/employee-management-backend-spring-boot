package com.asfak.employee_management_backend.auth.repository;

import com.asfak.employee_management_backend.auth.entity.PasswordResetOtp;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetOtpRepository
        extends JpaRepository<PasswordResetOtp, Long> {

    Optional<PasswordResetOtp>
    findFirstByEmailAndUsedFalseOrderByCreatedAtDesc(
            String email
    );

    void deleteByEmail(
            String email
    );
}