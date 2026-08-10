package com.asfak.employee_management_backend.role.service;

import com.asfak.employee_management_backend.role.dto.RoleResponse;

import java.util.List;

public interface RoleService {

    List<RoleResponse> getAllRoles();

}