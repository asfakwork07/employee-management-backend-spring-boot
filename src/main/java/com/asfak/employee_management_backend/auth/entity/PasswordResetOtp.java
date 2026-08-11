package com.asfak.employee_management_backend.auth.entity;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "password_reset_otps",
        indexes = {
                @Index(
                        name = "idx_password_reset_email",
                        columnList = "email"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetOtp {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    @Column(
            nullable = false,
            length = 150
    )
    private String email;

    @Column(
            nullable = false
    )
    private String otpHash;

    @Column(
            nullable = false
    )
    private LocalDateTime expiresAt;

    @Column(
            nullable = false
    )
    private Boolean used;

    @Column(
            nullable = false
    )
    private LocalDateTime createdAt;
}