package com.asfak.employee_management_backend.leave.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LeaveActionRequest {

    @NotBlank
    private String managerComment;

}

