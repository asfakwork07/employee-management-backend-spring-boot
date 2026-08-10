package com.asfak.employee_management_backend.service;

import com.asfak.employee_management_backend.dto.EmployeeAccountResponse;
import com.asfak.employee_management_backend.entity.Employee;
import com.asfak.employee_management_backend.entity.User;
import com.asfak.employee_management_backend.notification.service.NotificationService;
import com.asfak.employee_management_backend.repository.EmployeeRepository;
import com.asfak.employee_management_backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final EmployeeRepository employeeRepository;

    private final PasswordEncoder passwordEncoder;

    private final NotificationService notificationService;

    @Override
    public EmployeeAccountResponse createEmployeeAccount(
            Long employeeId
    ) {

        Employee employee =
                employeeRepository
                        .findById(employeeId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Employee not found"
                                )
                        );

        if (
                userRepository.existsByEmployeeId(
                        employeeId
                )
        ) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Login account already exists for this employee"
            );
        }

        if (
                userRepository
                        .findByEmail(
                                employee.getEmail()
                        )
                        .isPresent()
        ) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "User account already exists with this email"
            );
        }

        String temporaryPassword =
                generateTemporaryPassword();

        User user =
                new User();

        user.setName(
                employee.getFirstName()
                        + " "
                        + employee.getLastName()
        );

        user.setEmail(
                employee.getEmail()
        );

        user.setPassword(
                passwordEncoder.encode(
                        temporaryPassword
                )
        );

        user.setRole(
                "EMPLOYEE"
        );

        user.setEmployee(
                employee
        );

        user.setEnabled(
                true
        );

        User savedUser =
                userRepository.save(
                        user
                );

        notificationService.createNotification(
                savedUser,
                "Login Account Created",
                "Your employee login account has been created successfully.",
                "ACCOUNT_CREATED"
        );

        return new EmployeeAccountResponse(
                savedUser.getId(),
                employee.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole(),
                temporaryPassword,
                "Employee login account created successfully"
        );
    }

    @Override
    public EmployeeAccountResponse resetEmployeePassword(
            Long employeeId
    ) {

        Employee employee =
                employeeRepository
                        .findById(employeeId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Employee not found"
                                )
                        );

        User user =
                userRepository
                        .findByEmployeeId(
                                employeeId
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Login account does not exist for this employee"
                                )
                        );

        String temporaryPassword =
                generateTemporaryPassword();

        user.setPassword(
                passwordEncoder.encode(
                        temporaryPassword
                )
        );

        User savedUser =
                userRepository.save(
                        user
                );

        notificationService.createNotification(
                savedUser,
                "Password Reset",
                "Your login password has been reset by administrator.",
                "PASSWORD_RESET"
        );

        return new EmployeeAccountResponse(
                savedUser.getId(),
                employee.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole(),
                temporaryPassword,
                "Employee password reset successfully"
        );
    }

    @Override
    public EmployeeAccountResponse disableEmployeeAccount(
            Long employeeId
    ) {

        Employee employee =
                employeeRepository
                        .findById(employeeId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Employee not found"
                                )
                        );

        User user =
                userRepository
                        .findByEmployeeId(
                                employeeId
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Login account does not exist for this employee"
                                )
                        );

        if (
                !user.isEnabled()
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Employee login account is already disabled"
            );
        }

        user.setEnabled(
                false
        );

        User savedUser =
                userRepository.save(
                        user
                );

        notificationService.createNotification(
                savedUser,
                "Login Disabled",
                "Your login account has been disabled by administrator.",
                "ACCOUNT_DISABLED"
        );

        return new EmployeeAccountResponse(
                savedUser.getId(),
                employee.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole(),
                null,
                "Employee login account disabled successfully"
        );
    }

    @Override
    public EmployeeAccountResponse enableEmployeeAccount(
            Long employeeId
    ) {

        Employee employee =
                employeeRepository
                        .findById(employeeId)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Employee not found"
                                )
                        );

        User user =
                userRepository
                        .findByEmployeeId(
                                employeeId
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Login account does not exist for this employee"
                                )
                        );

        if (
                user.isEnabled()
        ) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Employee login account is already enabled"
            );
        }

        user.setEnabled(
                true
        );

        User savedUser =
                userRepository.save(
                        user
                );

        notificationService.createNotification(
                savedUser,
                "Login Enabled",
                "Your login account has been enabled by administrator.",
                "ACCOUNT_ENABLED"
        );

        return new EmployeeAccountResponse(
                savedUser.getId(),
                employee.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole(),
                null,
                "Employee login account enabled successfully"
        );
    }

    private String generateTemporaryPassword() {

        SecureRandom random =
                new SecureRandom();

        int number =
                100000
                        + random.nextInt(
                        900000
                );

        return "Emp@"
                + number;
    }
}