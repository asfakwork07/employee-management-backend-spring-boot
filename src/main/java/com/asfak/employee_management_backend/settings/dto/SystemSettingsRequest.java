package com.asfak.employee_management_backend.settings.dto;

import lombok.Data;

@Data
public class SystemSettingsRequest {

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