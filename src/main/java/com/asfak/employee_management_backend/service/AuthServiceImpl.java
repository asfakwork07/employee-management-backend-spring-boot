package com.asfak.employee_management_backend.service;

import com.asfak.employee_management_backend.dto.ChangePasswordRequest;
import com.asfak.employee_management_backend.dto.LoginRequest;
import com.asfak.employee_management_backend.dto.LoginResponse;
import com.asfak.employee_management_backend.entity.User;
import com.asfak.employee_management_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public LoginResponse login(
            LoginRequest request
    ) {

        User user =
                userRepository
                        .findByEmail(
                                request.getEmail()
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "Invalid email or password"
                                )
                        );

        if (!user.isEnabled()) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Your login account has been disabled. Please contact administrator."
            );
        }

        boolean matches =
                passwordEncoder.matches(
                        request.getPassword(),
                        user.getPassword()
                );


        if (!matches) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password"
            );
        }


        String token =
                jwtService.generateToken(
                        user.getEmail(),
                        user.getRole()
                );

        Long employeeId = null;
        String employeeName = null;
        String department = null;
        String designation = null;

        if (user.getEmployee() != null) {

            employeeId =
                    user.getEmployee().getId();

            employeeName =
                    user.getEmployee().getFirstName()
                            + " "
                            + user.getEmployee().getLastName();

            department =
                    user.getEmployee().getDepartment();

            designation =
                    user.getEmployee().getDesignation();
        }

        return new LoginResponse(
                token,
                "Login Successful",
                user.getRole(),
                user.getName(),
                user.getEmail(),
                employeeId,
                employeeName,
                department,
                designation
        );

    }

    @Override
    public void changePassword(
            String email,
            ChangePasswordRequest request
    ) {

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found"
                        )
                );

        boolean currentPasswordMatches =
                passwordEncoder.matches(
                        request.getCurrentPassword(),
                        user.getPassword()
                );

        if (!currentPasswordMatches) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Current password is incorrect"
            );
        }

        boolean samePassword =
                passwordEncoder.matches(
                        request.getNewPassword(),
                        user.getPassword()
                );

        if (samePassword) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "New password cannot be same as current password"
            );
        }

        user.setPassword(
                passwordEncoder.encode(
                        request.getNewPassword()
                )
        );

        userRepository.save(user);
    }
}
