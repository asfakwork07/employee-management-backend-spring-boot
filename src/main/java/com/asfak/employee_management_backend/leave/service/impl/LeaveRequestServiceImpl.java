//package com.asfak.employee_management_backend.leave.service.impl;
//
//import com.asfak.employee_management_backend.entity.Employee;
//import com.asfak.employee_management_backend.entity.User;
//import com.asfak.employee_management_backend.leave.dto.ApplyLeaveRequest;
//import com.asfak.employee_management_backend.leave.dto.LeaveResponse;
//import com.asfak.employee_management_backend.leave.entity.LeaveRequest;
//import com.asfak.employee_management_backend.leave.entity.LeaveType;
//import com.asfak.employee_management_backend.leave.mapper.LeaveMapper;
//import com.asfak.employee_management_backend.leave.repository.LeaveRequestRepository;
//import com.asfak.employee_management_backend.leave.repository.LeaveTypeRepository;
//import com.asfak.employee_management_backend.leave.service.LeaveRequestService;
//import com.asfak.employee_management_backend.notification.service.NotificationService;
//import com.asfak.employee_management_backend.repository.EmployeeRepository;
//import com.asfak.employee_management_backend.repository.UserRepository;
//import com.asfak.employee_management_backend.settings.entity.SystemSettings;
//import com.asfak.employee_management_backend.settings.repository.SystemSettingsRepository;
//
//import lombok.RequiredArgsConstructor;
//
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Service;
//import org.springframework.web.server.ResponseStatusException;
//
//import java.time.temporal.ChronoUnit;
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class LeaveRequestServiceImpl implements LeaveRequestService {
//
//    private final LeaveRequestRepository leaveRequestRepository;
//
//    private final EmployeeRepository employeeRepository;
//
//    private final LeaveTypeRepository leaveTypeRepository;
//
//    private final LeaveMapper leaveMapper;
//
//    private final UserRepository userRepository;
//
//    private final SystemSettingsRepository systemSettingsRepository;
//
//    private final NotificationService notificationService;
//
//    @Override
//    public LeaveResponse applyLeave(
//            ApplyLeaveRequest request,
//            String loggedInEmail
//    ) {
//
//        validateEmployeeOwnership(
//                request.getEmployeeId(),
//                loggedInEmail
//        );
//
//        Employee employee =
//                employeeRepository
//                        .findById(request.getEmployeeId())
//                        .orElseThrow(() ->
//                                new ResponseStatusException(
//                                        HttpStatus.NOT_FOUND,
//                                        "Employee not found"
//                                )
//                        );
//
//        LeaveType leaveType =
//                leaveTypeRepository
//                        .findById(request.getLeaveTypeId())
//                        .orElseThrow(() ->
//                                new ResponseStatusException(
//                                        HttpStatus.NOT_FOUND,
//                                        "Leave type not found"
//                                )
//                        );
//
//        if (
//                request.getFromDate()
//                        .isAfter(request.getToDate())
//        ) {
//
//            throw new ResponseStatusException(
//                    HttpStatus.BAD_REQUEST,
//                    "From Date cannot be after To Date"
//            );
//        }
//
//        long totalDays =
//                ChronoUnit.DAYS.between(
//                        request.getFromDate(),
//                        request.getToDate()
//                ) + 1;
//
//        if (totalDays <= 0) {
//
//            throw new ResponseStatusException(
//                    HttpStatus.BAD_REQUEST,
//                    "Invalid leave duration"
//            );
//        }
//
//        Long overlappingLeaves =
//                leaveRequestRepository
//                        .countOverlappingLeaves(
//                                employee.getId(),
//                                request.getFromDate(),
//                                request.getToDate()
//                        );
//
//        if (
//                overlappingLeaves != null &&
//                        overlappingLeaves > 0
//        ) {
//
//            throw new ResponseStatusException(
//                    HttpStatus.BAD_REQUEST,
//                    "Leave already exists for selected dates."
//            );
//        }
//
//        Integer usedDays =
//                leaveRequestRepository
//                        .getApprovedLeaveDays(
//                                employee.getId(),
//                                leaveType.getId()
//                        );
//
//        if (usedDays == null) {
//            usedDays = 0;
//        }
//
//        int leaveLimit =
//                getLeaveLimit(
//                        leaveType
//                );
//
//        int remainingDays =
//                Math.max(
//                        leaveLimit - usedDays,
//                        0
//                );
//
//        if (
//                totalDays > remainingDays
//        ) {
//
//            throw new ResponseStatusException(
//                    HttpStatus.BAD_REQUEST,
//                    "Only "
//                            + remainingDays
//                            + " "
//                            + leaveType.getName()
//                            + " leave(s) remaining."
//            );
//        }
//
//        LeaveRequest leaveRequest =
//                LeaveRequest.builder()
//                        .employee(employee)
//                        .leaveType(leaveType)
//                        .fromDate(request.getFromDate())
//                        .toDate(request.getToDate())
//                        .reason(request.getReason())
//                        .status("PENDING")
//                        .totalDays((int) totalDays)
//                        .build();
//
//        LeaveRequest saved =
//                leaveRequestRepository
//                        .save(leaveRequest);
//
//        notifyAdminsAboutNewLeave(
//                saved
//        );
//
//        return leaveMapper
//                .toLeaveResponse(saved);
//    }
//
//    @Override
//    public List<LeaveResponse> getAllLeaves() {
//
//        return leaveRequestRepository
//                .findAll()
//                .stream()
//                .map(
//                        leaveMapper::toLeaveResponse
//                )
//                .toList();
//    }
//
//    @Override
//    public List<LeaveResponse> getLeavesByEmployee(
//            Long employeeId,
//            String loggedInEmail
//    ) {
//
//        validateEmployeeOwnership(
//                employeeId,
//                loggedInEmail
//        );
//
//        return leaveRequestRepository
//                .findByEmployeeId(employeeId)
//                .stream()
//                .map(
//                        leaveMapper::toLeaveResponse
//                )
//                .toList();
//    }
//
//    @Override
//    public LeaveResponse approveLeave(
//            Long leaveId,
//            String loggedInEmail
//    ) {
//
//        validateAdmin(
//                loggedInEmail
//        );
//
//        LeaveRequest leave =
//                leaveRequestRepository
//                        .findById(leaveId)
//                        .orElseThrow(() ->
//                                new ResponseStatusException(
//                                        HttpStatus.NOT_FOUND,
//                                        "Leave not found"
//                                )
//                        );
//
//        if (
//                !"PENDING".equalsIgnoreCase(
//                        leave.getStatus()
//                )
//        ) {
//
//            throw new ResponseStatusException(
//                    HttpStatus.BAD_REQUEST,
//                    "Only pending leave can be approved"
//            );
//        }
//
//        validateLeaveBalanceBeforeApproval(
//                leave
//        );
//
//        leave.setStatus(
//                "APPROVED"
//        );
//
//        LeaveRequest saved =
//                leaveRequestRepository
//                        .save(leave);
//
//        notifyEmployeeAboutLeaveStatus(
//                saved,
//                true
//        );
//
//        return leaveMapper
//                .toLeaveResponse(saved);
//    }
//
//    @Override
//    public LeaveResponse rejectLeave(
//            Long leaveId,
//            String loggedInEmail
//    ) {
//
//        validateAdmin(
//                loggedInEmail
//        );
//
//        LeaveRequest leave =
//                leaveRequestRepository
//                        .findById(leaveId)
//                        .orElseThrow(() ->
//                                new ResponseStatusException(
//                                        HttpStatus.NOT_FOUND,
//                                        "Leave not found"
//                                )
//                        );
//
//        if (
//                !"PENDING".equalsIgnoreCase(
//                        leave.getStatus()
//                )
//        ) {
//
//            throw new ResponseStatusException(
//                    HttpStatus.BAD_REQUEST,
//                    "Only pending leave can be rejected"
//            );
//        }
//
//        leave.setStatus(
//                "REJECTED"
//        );
//
//        LeaveRequest saved =
//                leaveRequestRepository
//                        .save(leave);
//
//        notifyEmployeeAboutLeaveStatus(
//                saved,
//                false
//        );
//
//        return leaveMapper
//                .toLeaveResponse(saved);
//    }
//
//    private int getLeaveLimit(
//            LeaveType leaveType
//    ) {
//
//        int fallbackLimit =
//                leaveType.getMaxDays() != null
//                        ? leaveType.getMaxDays()
//                        : 0;
//
//        SystemSettings settings =
//                systemSettingsRepository
//                        .findAll()
//                        .stream()
//                        .findFirst()
//                        .orElse(null);
//
//        if (settings == null) {
//            return fallbackLimit;
//        }
//
//        String leaveName =
//                leaveType.getName() == null
//                        ? ""
//                        : leaveType.getName()
//                        .trim()
//                        .toUpperCase();
//
//        if (
//                leaveName.contains("CASUAL")
//        ) {
//
//            return settings.getCasualLeave() != null
//                    ? settings.getCasualLeave()
//                    : fallbackLimit;
//        }
//
//        if (
//                leaveName.contains("SICK")
//        ) {
//
//            return settings.getSickLeave() != null
//                    ? settings.getSickLeave()
//                    : fallbackLimit;
//        }
//
//        if (
//                leaveName.contains("EARNED")
//        ) {
//
//            return settings.getEarnedLeave() != null
//                    ? settings.getEarnedLeave()
//                    : fallbackLimit;
//        }
//
//        return fallbackLimit;
//    }
//
//    private void validateLeaveBalanceBeforeApproval(
//            LeaveRequest leave
//    ) {
//
//        Integer usedDays =
//                leaveRequestRepository
//                        .getApprovedLeaveDays(
//                                leave.getEmployee().getId(),
//                                leave.getLeaveType().getId()
//                        );
//
//        if (usedDays == null) {
//            usedDays = 0;
//        }
//
//        int leaveLimit =
//                getLeaveLimit(
//                        leave.getLeaveType()
//                );
//
//        int remainingDays =
//                Math.max(
//                        leaveLimit - usedDays,
//                        0
//                );
//
//        if (
//                leave.getTotalDays() > remainingDays
//        ) {
//
//            throw new ResponseStatusException(
//                    HttpStatus.BAD_REQUEST,
//                    "Leave cannot be approved. Only "
//                            + remainingDays
//                            + " "
//                            + leave.getLeaveType().getName()
//                            + " leave(s) remaining."
//            );
//        }
//    }
//
//    private void notifyAdminsAboutNewLeave(
//            LeaveRequest leave
//    ) {
//
//        List<User> admins =
//                userRepository
//                        .findByRoleIgnoreCase(
//                                "ADMIN"
//                        );
//
//        if (admins.isEmpty()) {
//            return;
//        }
//
//        String employeeName =
//                leave.getEmployee().getFirstName()
//                        + " "
//                        + leave.getEmployee().getLastName();
//
//        String leaveType =
//                leave.getLeaveType().getName();
//
//        String message =
//                employeeName
//                        + " applied for "
//                        + leaveType
//                        + " from "
//                        + leave.getFromDate()
//                        + " to "
//                        + leave.getToDate()
//                        + ".";
//
//        for (User admin : admins) {
//
//            notificationService.createNotification(
//                    admin,
//                    "New Leave Request",
//                    message,
//                    "LEAVE_APPLIED"
//            );
//        }
//    }
//
//    private void notifyEmployeeAboutLeaveStatus(
//            LeaveRequest leave,
//            boolean approved
//    ) {
//
//        User employeeUser =
//                userRepository
//                        .findByEmployeeId(
//                                leave.getEmployee()
//                                        .getId()
//                        )
//                        .orElse(null);
//
//        if (employeeUser == null) {
//            return;
//        }
//
//        String leaveType =
//                leave.getLeaveType()
//                        .getName();
//
//        String title =
//                approved
//                        ? "Leave Approved"
//                        : "Leave Rejected";
//
//        String message;
//
//        String type;
//
//        if (approved) {
//
//            message =
//                    "Your "
//                            + leaveType
//                            + " request from "
//                            + leave.getFromDate()
//                            + " to "
//                            + leave.getToDate()
//                            + " has been approved.";
//
//            type =
//                    "LEAVE_APPROVED";
//
//        } else {
//
//            message =
//                    "Your "
//                            + leaveType
//                            + " request from "
//                            + leave.getFromDate()
//                            + " to "
//                            + leave.getToDate()
//                            + " has been rejected.";
//
//            type =
//                    "LEAVE_REJECTED";
//        }
//
//        notificationService.createNotification(
//                employeeUser,
//                title,
//                message,
//                type
//        );
//    }
//
//    private User getLoggedInUser(
//            String email
//    ) {
//
//        return userRepository
//                .findByEmail(email)
//                .orElseThrow(() ->
//                        new ResponseStatusException(
//                                HttpStatus.UNAUTHORIZED,
//                                "Logged-in user not found"
//                        )
//                );
//    }
//
//    private void validateEmployeeOwnership(
//            Long employeeId,
//            String loggedInEmail
//    ) {
//
//        User user =
//                getLoggedInUser(
//                        loggedInEmail
//                );
//
//        if (
//                "ADMIN".equalsIgnoreCase(
//                        user.getRole()
//                )
//        ) {
//            return;
//        }
//
//        if (
//                user.getEmployee() == null
//        ) {
//
//            throw new ResponseStatusException(
//                    HttpStatus.FORBIDDEN,
//                    "User account is not linked with an employee"
//            );
//        }
//
//        Long loggedInEmployeeId =
//                user.getEmployee()
//                        .getId();
//
//        if (
//                !loggedInEmployeeId.equals(
//                        employeeId
//                )
//        ) {
//
//            throw new ResponseStatusException(
//                    HttpStatus.FORBIDDEN,
//                    "You can only access your own leave records"
//            );
//        }
//    }
//
//    private void validateAdmin(
//            String loggedInEmail
//    ) {
//
//        User user =
//                getLoggedInUser(
//                        loggedInEmail
//                );
//
//        if (
//                !"ADMIN".equalsIgnoreCase(
//                        user.getRole()
//                )
//        ) {
//
//            throw new ResponseStatusException(
//                    HttpStatus.FORBIDDEN,
//                    "Only admin can approve or reject leave requests"
//            );
//        }
//    }
//}

package com.asfak.employee_management_backend.leave.service.impl;

import com.asfak.employee_management_backend.email.service.EmailService;
import com.asfak.employee_management_backend.entity.Employee;
import com.asfak.employee_management_backend.entity.User;
import com.asfak.employee_management_backend.holiday.repository.HolidayRepository;
import com.asfak.employee_management_backend.leave.dto.ApplyLeaveRequest;
import com.asfak.employee_management_backend.leave.dto.LeaveResponse;
import com.asfak.employee_management_backend.leave.entity.LeaveRequest;
import com.asfak.employee_management_backend.leave.entity.LeaveType;
import com.asfak.employee_management_backend.leave.mapper.LeaveMapper;
import com.asfak.employee_management_backend.leave.repository.LeaveRequestRepository;
import com.asfak.employee_management_backend.leave.repository.LeaveTypeRepository;
import com.asfak.employee_management_backend.leave.service.LeaveRequestService;
import com.asfak.employee_management_backend.notification.service.NotificationService;
import com.asfak.employee_management_backend.repository.EmployeeRepository;
import com.asfak.employee_management_backend.repository.UserRepository;
import com.asfak.employee_management_backend.settings.entity.SystemSettings;
import com.asfak.employee_management_backend.settings.repository.SystemSettingsRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaveRequestServiceImpl implements LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;

    private final EmployeeRepository employeeRepository;

    private final LeaveTypeRepository leaveTypeRepository;

    private final LeaveMapper leaveMapper;

    private final UserRepository userRepository;

    private final SystemSettingsRepository systemSettingsRepository;

    private final NotificationService notificationService;

    private final HolidayRepository holidayRepository;

    private final EmailService emailService;

    @Override
    public LeaveResponse applyLeave(
            ApplyLeaveRequest request,
            String loggedInEmail
    ) {

        validateEmployeeOwnership(
                request.getEmployeeId(),
                loggedInEmail
        );

        Employee employee =
                employeeRepository
                        .findById(
                                request.getEmployeeId()
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Employee not found"
                                )
                        );

        LeaveType leaveType =
                leaveTypeRepository
                        .findById(
                                request.getLeaveTypeId()
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Leave type not found"
                                )
                        );

        if (
                request.getFromDate() == null ||
                        request.getToDate() == null
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "From Date and To Date are required"
            );
        }

        if (
                request.getFromDate()
                        .isAfter(
                                request.getToDate()
                        )
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "From Date cannot be after To Date"
            );
        }

        int totalDays =
                calculateLeaveDays(
                        request.getFromDate(),
                        request.getToDate()
                );

        if (
                totalDays <= 0
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Selected dates contain only weekends or company holidays."
            );
        }

        Long overlappingLeaves =
                leaveRequestRepository
                        .countOverlappingLeaves(
                                employee.getId(),
                                request.getFromDate(),
                                request.getToDate()
                        );

        if (
                overlappingLeaves != null &&
                        overlappingLeaves > 0
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Leave already exists for selected dates."
            );
        }

        Integer usedDays =
                leaveRequestRepository
                        .getApprovedLeaveDays(
                                employee.getId(),
                                leaveType.getId()
                        );

        if (
                usedDays == null
        ) {

            usedDays = 0;
        }

        int leaveLimit =
                getLeaveLimit(
                        leaveType
                );

        int remainingDays =
                Math.max(
                        leaveLimit - usedDays,
                        0
                );

        if (
                totalDays > remainingDays
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only "
                            + remainingDays
                            + " "
                            + leaveType.getName()
                            + " leave(s) remaining."
            );
        }

        LeaveRequest leaveRequest =
                LeaveRequest
                        .builder()
                        .employee(
                                employee
                        )
                        .leaveType(
                                leaveType
                        )
                        .fromDate(
                                request.getFromDate()
                        )
                        .toDate(
                                request.getToDate()
                        )
                        .reason(
                                request.getReason()
                        )
                        .status(
                                "PENDING"
                        )
                        .totalDays(
                                totalDays
                        )
                        .build();

        LeaveRequest saved =
                leaveRequestRepository
                        .save(
                                leaveRequest
                        );

        notifyAdminsAboutNewLeave(
                saved
        );

        return leaveMapper
                .toLeaveResponse(
                        saved
                );
    }

    @Override
    public List<LeaveResponse> getAllLeaves() {

        return leaveRequestRepository
                .findAll()
                .stream()
                .map(
                        leaveMapper::toLeaveResponse
                )
                .toList();
    }

    @Override
    public List<LeaveResponse> getLeavesByEmployee(
            Long employeeId,
            String loggedInEmail
    ) {

        validateEmployeeOwnership(
                employeeId,
                loggedInEmail
        );

        return leaveRequestRepository
                .findByEmployeeId(
                        employeeId
                )
                .stream()
                .map(
                        leaveMapper::toLeaveResponse
                )
                .toList();
    }

    @Override
    public LeaveResponse approveLeave(
            Long leaveId,
            String loggedInEmail
    ) {

        validateAdmin(
                loggedInEmail
        );

        LeaveRequest leave =
                leaveRequestRepository
                        .findById(
                                leaveId
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Leave not found"
                                )
                        );

        if (
                !"PENDING".equalsIgnoreCase(
                        leave.getStatus()
                )
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only pending leave can be approved"
            );
        }

        validateLeaveBalanceBeforeApproval(
                leave
        );

        leave.setStatus(
                "APPROVED"
        );

        LeaveRequest saved =
                leaveRequestRepository
                        .save(
                                leave
                        );

        notifyEmployeeAboutLeaveStatus(
                saved,
                true
        );

        return leaveMapper
                .toLeaveResponse(
                        saved
                );
    }

    @Override
    public LeaveResponse rejectLeave(
            Long leaveId,
            String loggedInEmail
    ) {

        validateAdmin(
                loggedInEmail
        );

        LeaveRequest leave =
                leaveRequestRepository
                        .findById(
                                leaveId
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Leave not found"
                                )
                        );

        if (
                !"PENDING".equalsIgnoreCase(
                        leave.getStatus()
                )
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only pending leave can be rejected"
            );
        }

        leave.setStatus(
                "REJECTED"
        );

        LeaveRequest saved =
                leaveRequestRepository
                        .save(
                                leave
                        );

        notifyEmployeeAboutLeaveStatus(
                saved,
                false
        );

        return leaveMapper
                .toLeaveResponse(
                        saved
                );
    }

    private int calculateLeaveDays(
            LocalDate fromDate,
            LocalDate toDate
    ) {

        int leaveDays =
                0;

        LocalDate currentDate =
                fromDate;

        while (
                !currentDate.isAfter(
                        toDate
                )
        ) {

            boolean saturday =
                    currentDate.getDayOfWeek()
                            == DayOfWeek.SATURDAY;

            boolean sunday =
                    currentDate.getDayOfWeek()
                            == DayOfWeek.SUNDAY;

            boolean weekend =
                    saturday ||
                            sunday;

            boolean holiday =
                    holidayRepository
                            .existsByHolidayDate(
                                    currentDate
                            );

            if (
                    !weekend &&
                            !holiday
            ) {

                leaveDays++;
            }

            currentDate =
                    currentDate.plusDays(
                            1
                    );
        }

        return leaveDays;
    }

    private int getLeaveLimit(
            LeaveType leaveType
    ) {

        int fallbackLimit =
                leaveType.getMaxDays() != null
                        ? leaveType.getMaxDays()
                        : 0;

        SystemSettings settings =
                systemSettingsRepository
                        .findAll()
                        .stream()
                        .findFirst()
                        .orElse(null);

        if (
                settings == null
        ) {

            return fallbackLimit;
        }

        String leaveName =
                leaveType.getName() == null
                        ? ""
                        : leaveType
                        .getName()
                        .trim()
                        .toUpperCase();

        if (
                leaveName.contains(
                        "CASUAL"
                )
        ) {

            return settings.getCasualLeave() != null
                    ? settings.getCasualLeave()
                    : fallbackLimit;
        }

        if (
                leaveName.contains(
                        "SICK"
                )
        ) {

            return settings.getSickLeave() != null
                    ? settings.getSickLeave()
                    : fallbackLimit;
        }

        if (
                leaveName.contains(
                        "EARNED"
                )
        ) {

            return settings.getEarnedLeave() != null
                    ? settings.getEarnedLeave()
                    : fallbackLimit;
        }

        return fallbackLimit;
    }

    private void validateLeaveBalanceBeforeApproval(
            LeaveRequest leave
    ) {

        Integer usedDays =
                leaveRequestRepository
                        .getApprovedLeaveDays(
                                leave.getEmployee()
                                        .getId(),
                                leave.getLeaveType()
                                        .getId()
                        );

        if (
                usedDays == null
        ) {

            usedDays = 0;
        }

        int leaveLimit =
                getLeaveLimit(
                        leave.getLeaveType()
                );

        int remainingDays =
                Math.max(
                        leaveLimit - usedDays,
                        0
                );

        if (
                leave.getTotalDays() > remainingDays
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Leave cannot be approved. Only "
                            + remainingDays
                            + " "
                            + leave.getLeaveType()
                            .getName()
                            + " leave(s) remaining."
            );
        }
    }

    private void notifyAdminsAboutNewLeave(
            LeaveRequest leave
    ) {

        List<User> admins =
                userRepository
                        .findByRoleIgnoreCase(
                                "ADMIN"
                        );

        if (
                admins.isEmpty()
        ) {

            return;
        }

        String employeeName =
                leave.getEmployee()
                        .getFirstName()
                        + " "
                        + leave.getEmployee()
                        .getLastName();

        String leaveType =
                leave.getLeaveType()
                        .getName();

        String message =
                employeeName
                        + " applied for "
                        + leaveType
                        + " from "
                        + leave.getFromDate()
                        + " to "
                        + leave.getToDate()
                        + " ("
                        + leave.getTotalDays()
                        + " working day(s)).";

        for (
                User admin : admins
        ) {

            notificationService
                    .createNotification(
                            admin,
                            "New Leave Request",
                            message,
                            "LEAVE_APPLIED"
                    );

            if (
                    admin.getEmail() != null &&
                            !admin.getEmail().isBlank()
            ) {

                emailService.sendEmail(
                        admin.getEmail(),
                        "New Leave Request - " + employeeName,
                        "Hello Admin,\n\n"
                                + employeeName
                                + " has applied for "
                                + leaveType
                                + ".\n\n"
                                + "From: "
                                + leave.getFromDate()
                                + "\n"
                                + "To: "
                                + leave.getToDate()
                                + "\n"
                                + "Working Days: "
                                + leave.getTotalDays()
                                + "\n"
                                + "Reason: "
                                + (
                                leave.getReason() != null
                                        ? leave.getReason()
                                        : "--"
                        )
                                + "\n\n"
                                + "Please review the leave request in the Employee Management System.\n\n"
                                + "Regards,\n"
                                + "Employee Management System"
                );
            }
        }
    }

    private void notifyEmployeeAboutLeaveStatus(
            LeaveRequest leave,
            boolean approved
    ) {

        User employeeUser =
                userRepository
                        .findByEmployeeId(
                                leave.getEmployee()
                                        .getId()
                        )
                        .orElse(null);

        if (
                employeeUser == null
        ) {

            return;
        }

        String leaveType =
                leave.getLeaveType()
                        .getName();

        String title =
                approved
                        ? "Leave Approved"
                        : "Leave Rejected";

        String message;

        String type;

        if (
                approved
        ) {

            message =
                    "Your "
                            + leaveType
                            + " request from "
                            + leave.getFromDate()
                            + " to "
                            + leave.getToDate()
                            + " for "
                            + leave.getTotalDays()
                            + " working day(s) has been approved.";

            type =
                    "LEAVE_APPROVED";

        } else {

            message =
                    "Your "
                            + leaveType
                            + " request from "
                            + leave.getFromDate()
                            + " to "
                            + leave.getToDate()
                            + " for "
                            + leave.getTotalDays()
                            + " working day(s) has been rejected.";

            type =
                    "LEAVE_REJECTED";
        }

        notificationService
                .createNotification(
                        employeeUser,
                        title,
                        message,
                        type
                );
        if (
                employeeUser.getEmail() != null &&
                        !employeeUser.getEmail().isBlank()
        ) {

            emailService.sendEmail(
                    employeeUser.getEmail(),
                    title,
                    "Hello "
                            + leave.getEmployee().getFirstName()
                            + ",\n\n"
                            + message
                            + "\n\n"
                            + "Regards,\n"
                            + "Employee Management System"
            );
        }
    }

    private User getLoggedInUser(
            String email
    ) {

        return userRepository
                .findByEmail(
                        email
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Logged-in user not found"
                        )
                );
    }

    private void validateEmployeeOwnership(
            Long employeeId,
            String loggedInEmail
    ) {

        User user =
                getLoggedInUser(
                        loggedInEmail
                );

        if (
                "ADMIN".equalsIgnoreCase(
                        user.getRole()
                )
        ) {

            return;
        }

        if (
                user.getEmployee() == null
        ) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "User account is not linked with an employee"
            );
        }

        Long loggedInEmployeeId =
                user.getEmployee()
                        .getId();

        if (
                !loggedInEmployeeId.equals(
                        employeeId
                )
        ) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You can only access your own leave records"
            );
        }
    }

    private void validateAdmin(
            String loggedInEmail
    ) {

        User user =
                getLoggedInUser(
                        loggedInEmail
                );

        if (
                !"ADMIN".equalsIgnoreCase(
                        user.getRole()
                )
        ) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only admin can approve or reject leave requests"
            );
        }
    }
}