package com.asfak.employee_management_backend.leave.service.impl;

import com.asfak.employee_management_backend.leave.dto.LeaveTypeResponse;
import com.asfak.employee_management_backend.leave.entity.LeaveType;
import com.asfak.employee_management_backend.leave.mapper.LeaveMapper;
import com.asfak.employee_management_backend.leave.repository.LeaveTypeRepository;
import com.asfak.employee_management_backend.leave.service.LeaveTypeService;
import com.asfak.employee_management_backend.settings.entity.SystemSettings;
import com.asfak.employee_management_backend.settings.repository.SystemSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaveTypeServiceImpl implements LeaveTypeService {

    private final LeaveTypeRepository leaveTypeRepository;

    private final LeaveMapper leaveMapper;

    private final SystemSettingsRepository systemSettingsRepository;

    @Override
    public List<LeaveTypeResponse> getAllLeaveTypes() {

        SystemSettings settings =
                systemSettingsRepository
                        .findAll()
                        .stream()
                        .findFirst()
                        .orElse(null);

        return leaveTypeRepository
                .findAll()
                .stream()
                .map(leaveType -> {

                    LeaveTypeResponse response =
                            leaveMapper.toLeaveTypeResponse(
                                    leaveType
                            );

                    response.setMaxDays(
                            getConfiguredMaxDays(
                                    leaveType,
                                    settings
                            )
                    );

                    return response;
                })
                .toList();
    }

    private Integer getConfiguredMaxDays(
            LeaveType leaveType,
            SystemSettings settings
    ) {

        Integer fallback =
                leaveType.getMaxDays();

        if (settings == null) {
            return fallback;
        }

        String leaveName =
                leaveType.getName() == null
                        ? ""
                        : leaveType
                        .getName()
                        .trim()
                        .toUpperCase();

        if (leaveName.contains("CASUAL")) {

            return settings.getCasualLeave() != null
                    ? settings.getCasualLeave()
                    : fallback;
        }

        if (leaveName.contains("SICK")) {

            return settings.getSickLeave() != null
                    ? settings.getSickLeave()
                    : fallback;
        }

        if (leaveName.contains("EARNED")) {

            return settings.getEarnedLeave() != null
                    ? settings.getEarnedLeave()
                    : fallback;
        }

        return fallback;
    }
}