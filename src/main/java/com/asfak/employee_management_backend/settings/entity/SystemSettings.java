package com.asfak.employee_management_backend.settings.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "system_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String companyName;

    private String companyEmail;

    private String companyPhone;

    private String companyAddress;

    private String officeStartTime;

    private String officeEndTime;

    private Integer workingHours;

    private Integer gracePeriod;

    private Integer casualLeave;

    private Integer sickLeave;

    private Integer earnedLeave;

    private Double pfPercentage;

    private Double hraPercentage;

    private Double professionalTax;

    private Double defaultAllowance;

    private Integer minimumPasswordLength;

    private Integer sessionTimeout;

    private Boolean forcePasswordChange;
}