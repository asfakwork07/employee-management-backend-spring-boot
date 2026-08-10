package com.asfak.employee_management_backend.leave.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LeaveTypeResponse {

    private Long id;

    private String name;

    private Integer maxDays;

}