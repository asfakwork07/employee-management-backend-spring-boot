package com.asfak.employee_management_backend.settings.controller;

import com.asfak.employee_management_backend.settings.dto.SystemSettingsRequest;
import com.asfak.employee_management_backend.settings.dto.SystemSettingsResponse;
import com.asfak.employee_management_backend.settings.service.SystemSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SystemSettingsController {

    private final SystemSettingsService
            systemSettingsService;

    @GetMapping
    public SystemSettingsResponse getSettings() {

        return systemSettingsService
                .getSettings();
    }

    @PutMapping
    public SystemSettingsResponse updateSettings(
            @RequestBody SystemSettingsRequest request
    ) {

        return systemSettingsService
                .updateSettings(request);
    }
}