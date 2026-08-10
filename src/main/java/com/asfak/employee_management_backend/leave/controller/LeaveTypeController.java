//package com.asfak.employee_management_backend.leave.controller;
//
//import com.asfak.employee_management_backend.leave.entity.LeaveType;
//import com.asfak.employee_management_backend.leave.service.LeaveTypeService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/leave-types")
//@RequiredArgsConstructor
//@CrossOrigin(origins = "*")
//public class LeaveTypeController {
//
//    private final LeaveTypeService leaveTypeService;
//
//    @GetMapping
//    public List<LeaveType> getAllLeaveTypes() {
//        return leaveTypeService.getAllLeaveTypes();
//    }
//}
package com.asfak.employee_management_backend.leave.controller;

import com.asfak.employee_management_backend.leave.dto.LeaveTypeResponse;
import com.asfak.employee_management_backend.leave.service.LeaveTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leave-types")
@RequiredArgsConstructor
@CrossOrigin("*")
public class LeaveTypeController {

    private final LeaveTypeService leaveTypeService;

    @GetMapping
    public List<LeaveTypeResponse> getAllLeaveTypes() {

        return leaveTypeService.getAllLeaveTypes();

    }

}