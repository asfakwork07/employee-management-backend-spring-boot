package com.asfak.employee_management_backend.settings.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SystemSettingsResponse {

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