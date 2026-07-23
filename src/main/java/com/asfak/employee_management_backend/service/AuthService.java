package com.asfak.employee_management_backend.service;

import com.asfak.employee_management_backend.dto.LoginRequest;
import com.asfak.employee_management_backend.dto.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

}