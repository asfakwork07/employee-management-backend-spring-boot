package com.asfak.employee_management_backend.controller;

import com.asfak.employee_management_backend.dto.LoginRequest;
import com.asfak.employee_management_backend.dto.LoginResponse;
import com.asfak.employee_management_backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.asfak.employee_management_backend.dto.ChangePasswordRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request) {

        return ResponseEntity.ok(authService.login(request));
    }
    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication
    ) {

        String email =
                authentication.getName();

        authService.changePassword(
                email,
                request
        );

        return ResponseEntity.ok(
                "Password changed successfully"
        );
    }


}