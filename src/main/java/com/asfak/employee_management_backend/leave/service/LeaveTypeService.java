//package com.asfak.employee_management_backend.leave.service;
//
//import com.asfak.employee_management_backend.leave.entity.LeaveType;
//
//import java.util.List;
//
//public interface LeaveTypeService {
//
//    List<LeaveType> getAllLeaveTypes();
//
//}
package com.asfak.employee_management_backend.leave.service;

import com.asfak.employee_management_backend.leave.dto.LeaveTypeResponse;

import java.util.List;

public interface LeaveTypeService {

    List<LeaveTypeResponse> getAllLeaveTypes();

}