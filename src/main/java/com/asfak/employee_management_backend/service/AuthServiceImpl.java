package com.asfak.employee_management_backend.service;

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
public LoginResponse login(LoginRequest request) {

    User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() ->
                    new ResponseStatusException(
                            HttpStatus.UNAUTHORIZED,
                            "Invalid email or password"
                    )
            );

    // Debug logs
    System.out.println("Entered Password: " + request.getPassword());
    System.out.println("DB Password: " + user.getPassword());

    boolean matches = passwordEncoder.matches(
            request.getPassword(),
            user.getPassword()
    );

    System.out.println("Password Matches: " + matches);

    if (!matches) {
        throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Invalid email or password"
        );
    }

    LoginResponse response = new LoginResponse();
    response.setMessage("Login Successful");
    String token = jwtService.generateToken(user.getEmail());

    response.setToken(token);

    return response;
}
}