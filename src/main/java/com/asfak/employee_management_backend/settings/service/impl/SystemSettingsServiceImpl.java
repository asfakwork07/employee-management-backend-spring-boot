package com.asfak.employee_management_backend.settings.service.impl;

import com.asfak.employee_management_backend.settings.dto.SystemSettingsRequest;
import com.asfak.employee_management_backend.settings.dto.SystemSettingsResponse;
import com.asfak.employee_management_backend.settings.entity.SystemSettings;
import com.asfak.employee_management_backend.settings.repository.SystemSettingsRepository;
import com.asfak.employee_management_backend.settings.service.SystemSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SystemSettingsServiceImpl
        implements SystemSettingsService {

    private final SystemSettingsRepository
            systemSettingsRepository;

    @Override
    public SystemSettingsResponse getSettings() {

        SystemSettings settings =
                systemSettingsRepository
                        .findAll()
                        .stream()
                        .findFirst()
                        .orElseGet(
                                this::createDefaultSettings
                        );

        return mapToResponse(settings);
    }

    @Override
    public SystemSettingsResponse updateSettings(
            SystemSettingsRequest request
    ) {

        SystemSettings settings =
                systemSettingsRepository
                        .findAll()
                        .stream()
                        .findFirst()
                        .orElseGet(
                                this::createDefaultSettings
                        );

        settings.setCompanyName(
                request.getCompanyName()
        );

        settings.setCompanyEmail(
                request.getCompanyEmail()
        );

        settings.setCompanyPhone(
                request.getCompanyPhone()
        );

        settings.setCompanyAddress(
                request.getCompanyAddress()
        );

        settings.setOfficeStartTime(
                request.getOfficeStartTime()
        );

        settings.setOfficeEndTime(
                request.getOfficeEndTime()
        );

        settings.setWorkingHours(
                request.getWorkingHours()
        );

        settings.setGracePeriod(
                request.getGracePeriod()
        );

        settings.setCasualLeave(
                request.getCasualLeave()
        );

        settings.setSickLeave(
                request.getSickLeave()
        );

        settings.setEarnedLeave(
                request.getEarnedLeave()
        );

        settings.setPfPercentage(
                request.getPfPercentage()
        );

        settings.setHraPercentage(
                request.getHraPercentage()
        );

        settings.setProfessionalTax(
                request.getProfessionalTax()
        );

        settings.setDefaultAllowance(
                request.getDefaultAllowance()
        );

        settings.setMinimumPasswordLength(
                request.getMinimumPasswordLength()
        );

        settings.setSessionTimeout(
                request.getSessionTimeout()
        );

        settings.setForcePasswordChange(
                request.getForcePasswordChange()
        );

        SystemSettings saved =
                systemSettingsRepository
                        .save(settings);

        return mapToResponse(saved);
    }

    private SystemSettings createDefaultSettings() {

        SystemSettings settings =
                SystemSettings
                        .builder()
                        .companyName(
                                "Employee Management System"
                        )
                        .companyEmail(
                                "admin@company.com"
                        )
                        .companyPhone("")
                        .companyAddress("")
                        .officeStartTime(
                                "09:30"
                        )
                        .officeEndTime(
                                "18:30"
                        )
                        .workingHours(9)
                        .gracePeriod(15)
                        .casualLeave(12)
                        .sickLeave(10)
                        .earnedLeave(15)
                        .pfPercentage(12.0)
                        .hraPercentage(40.0)
                        .professionalTax(200.0)
                        .defaultAllowance(0.0)
                        .minimumPasswordLength(8)
                        .sessionTimeout(60)
                        .forcePasswordChange(false)
                        .build();

        return systemSettingsRepository
                .save(settings);
    }

    private SystemSettingsResponse mapToResponse(
            SystemSettings settings
    ) {

        return SystemSettingsResponse
                .builder()
                .id(settings.getId())
                .companyName(
                        settings.getCompanyName()
                )
                .companyEmail(
                        settings.getCompanyEmail()
                )
                .companyPhone(
                        settings.getCompanyPhone()
                )
                .companyAddress(
                        settings.getCompanyAddress()
                )
                .officeStartTime(
                        settings.getOfficeStartTime()
                )
                .officeEndTime(
                        settings.getOfficeEndTime()
                )
                .workingHours(
                        settings.getWorkingHours()
                )
                .gracePeriod(
                        settings.getGracePeriod()
                )
                .casualLeave(
                        settings.getCasualLeave()
                )
                .sickLeave(
                        settings.getSickLeave()
                )
                .earnedLeave(
                        settings.getEarnedLeave()
                )
                .pfPercentage(
                        settings.getPfPercentage()
                )
                .hraPercentage(
                        settings.getHraPercentage()
                )
                .professionalTax(
                        settings.getProfessionalTax()
                )
                .defaultAllowance(
                        settings.getDefaultAllowance()
                )
                .minimumPasswordLength(
                        settings.getMinimumPasswordLength()
                )
                .sessionTimeout(
                        settings.getSessionTimeout()
                )
                .forcePasswordChange(
                        settings.getForcePasswordChange()
                )
                .build();
    }
}