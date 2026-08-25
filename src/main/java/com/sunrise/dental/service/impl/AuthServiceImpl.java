package com.sunrise.dental.service.impl;

import com.sunrise.dental.audit.AuditService;
import com.sunrise.dental.dto.request.LoginRequest;
import com.sunrise.dental.dto.request.RegisterUserRequest;
import com.sunrise.dental.dto.response.LoginResponse;
import com.sunrise.dental.dto.response.UserResponse;
import com.sunrise.dental.entity.User;
import com.sunrise.dental.enums.Role;
import com.sunrise.dental.exception.BusinessRuleException;
import com.sunrise.dental.exception.DuplicateResourceException;
import com.sunrise.dental.mapper.UserMapper;
import com.sunrise.dental.repository.UserRepository;
import com.sunrise.dental.security.JwtService;
import com.sunrise.dental.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Authentication business logic: login, token generation, refresh and
 * current-user lookup.
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AuditService auditService;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request, String ipAddress) {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        User user = userRepository.findByUsernameOrEmail(request.getUsername())
                .orElseThrow(() -> new BusinessRuleException("Invalid username or password"));

        user.setFailedAttempts(0);
        user.setLastLoginDate(LocalDateTime.now());
        userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        auditService.log("LOGIN", "User", user.getId(), "User logged in from " + ipAddress);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(java.time.Duration.ofMillis(com.sunrise.dental.constant.SecurityConstants.ACCESS_TOKEN_VALIDITY_MS).getSeconds())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .loginTime(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional
    public UserResponse registerFirstAdmin(RegisterUserRequest request) {
        if (userRepository.count() > 0) {
            throw new BusinessRuleException("Admin registration is only available on a fresh system.");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already taken: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }
        if (request.getRole() != Role.ADMIN) {
            throw new BusinessRuleException("The first account must have the ADMIN role.");
        }
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .fullName(request.getFullName())
                .contactNumber(request.getContactNumber())
                .role(Role.ADMIN)
                .active(true)
                .build();
        userRepository.save(user);
        auditService.log("REGISTER", "User", user.getId(), "First admin account created");
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse refreshAccessToken(String refreshToken) {
        String username = jwtService.extractUsernameFromRefreshToken(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessRuleException("User no longer exists"));
        if (!jwtService.isRefreshTokenValid(refreshToken, user.getUsername())) {
            throw new BusinessRuleException("Refresh token is invalid or expired. Please log in again.");
        }
        String newAccessToken = jwtService.generateAccessToken(user);
        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .tokenType("Bearer")
                .expiresIn(java.time.Duration.ofMillis(com.sunrise.dental.constant.SecurityConstants.ACCESS_TOKEN_VALIDITY_MS).getSeconds())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .loginTime(LocalDateTime.now())
                .build();
    }

    @Override
    public void logout() {
        // Stateless JWT: client clears cookies; audit for traceability.
        auditService.log("LOGOUT", "User", null, "User logged out");
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse me(String username) {
        User user = userRepository.findByUsernameOrEmail(username)
                .orElseThrow(() -> new com.sunrise.dental.exception.ResourceNotFoundException("User not found"));
        return userMapper.toResponse(user);
    }
}
