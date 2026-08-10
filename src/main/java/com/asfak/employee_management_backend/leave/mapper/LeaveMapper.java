package com.asfak.employee_management_backend.leave.mapper;

import com.asfak.employee_management_backend.leave.dto.LeaveResponse;
import com.asfak.employee_management_backend.leave.dto.LeaveTypeResponse;
import com.asfak.employee_management_backend.leave.entity.LeaveRequest;
import com.asfak.employee_management_backend.leave.entity.LeaveType;
import org.springframework.stereotype.Component;

@Component
public class LeaveMapper {

    public LeaveTypeResponse toLeaveTypeResponse(LeaveType leaveType) {

        return LeaveTypeResponse.builder()
                .id(leaveType.getId())
                .name(leaveType.getName())
                .maxDays(leaveType.getMaxDays())
                .build();
    }

    public LeaveResponse toLeaveResponse(LeaveRequest leaveRequest) {

        return LeaveResponse.builder()
                .id(leaveRequest.getId())
                .employeeName(
                        leaveRequest.getEmployee().getFirstName()
                                + " "
                                + leaveRequest.getEmployee().getLastName()
                )
                .leaveType(
                        leaveRequest.getLeaveType().getName()
                )
                .fromDate(leaveRequest.getFromDate())
                .toDate(leaveRequest.getToDate())
                .totalDays(leaveRequest.getTotalDays())
                .status(leaveRequest.getStatus())
                .build();
    }

}