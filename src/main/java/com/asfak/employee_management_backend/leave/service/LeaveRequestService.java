package com.asfak.employee_management_backend.leave.service;

import com.asfak.employee_management_backend.leave.dto.ApplyLeaveRequest;
import com.asfak.employee_management_backend.leave.dto.LeaveResponse;
import jakarta.validation.Valid;

import java.util.List;

public interface LeaveRequestService {

    LeaveResponse applyLeave(
            @Valid ApplyLeaveRequest request,
            String loggedInEmail
    );

    List<LeaveResponse> getAllLeaves();

    List<LeaveResponse> getLeavesByEmployee(
            Long employeeId,
            String loggedInEmail
    );

    LeaveResponse approveLeave(
            Long leaveId,
            String loggedInEmail
    );

    LeaveResponse rejectLeave(
            Long leaveId,
            String loggedInEmail
    );
}