package com.asfak.employee_management_backend.leave.controller;

import com.asfak.employee_management_backend.leave.dto.ApplyLeaveRequest;
import com.asfak.employee_management_backend.leave.dto.LeaveResponse;
import com.asfak.employee_management_backend.leave.service.LeaveRequestService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    @GetMapping
    public List<LeaveResponse> getAllLeaves() {
        return leaveRequestService.getAllLeaves();
    }

    @GetMapping("/employee/{employeeId}")
    public List<LeaveResponse> getEmployeeLeave(
            @PathVariable Long employeeId,
            Authentication authentication
    ) {

        return leaveRequestService.getLeavesByEmployee(
                employeeId,
                authentication.getName()
        );
    }

    @PostMapping
    public LeaveResponse applyLeave(
            @Valid @RequestBody ApplyLeaveRequest request,
            Authentication authentication
    ) {

        return leaveRequestService.applyLeave(
                request,
                authentication.getName()
        );
    }

    @PutMapping("/{id}/approve")
    public LeaveResponse approveLeave(
            @PathVariable Long id,
            Authentication authentication
    ) {

        return leaveRequestService.approveLeave(
                id,
                authentication.getName()
        );
    }

    @PutMapping("/{id}/reject")
    public LeaveResponse rejectLeave(
            @PathVariable Long id,
            Authentication authentication
    ) {

        return leaveRequestService.rejectLeave(
                id,
                authentication.getName()
        );
    }
}