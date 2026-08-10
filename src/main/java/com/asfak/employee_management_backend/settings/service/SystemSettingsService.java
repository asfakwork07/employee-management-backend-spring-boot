package com.asfak.employee_management_backend.settings.service;

import com.asfak.employee_management_backend.settings.dto.SystemSettingsRequest;
import com.asfak.employee_management_backend.settings.dto.SystemSettingsResponse;

public interface SystemSettingsService {

    SystemSettingsResponse getSettings();

    SystemSettingsResponse updateSettings(
            SystemSettingsRequest request
    );
}