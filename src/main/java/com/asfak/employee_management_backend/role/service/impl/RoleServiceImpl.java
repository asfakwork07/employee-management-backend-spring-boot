package com.asfak.employee_management_backend.role.service.impl;

import com.asfak.employee_management_backend.role.dto.RoleResponse;
import com.asfak.employee_management_backend.role.entity.Role;
import com.asfak.employee_management_backend.role.repository.RoleRepository;
import com.asfak.employee_management_backend.role.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    public List<RoleResponse> getAllRoles() {

        List<Role> roles = roleRepository.findAll();

        return roles.stream()
                .map(role -> RoleResponse.builder()
                        .id(role.getId())
                        .name(role.getName())
                        .build())
                .collect(Collectors.toList());
    }
}