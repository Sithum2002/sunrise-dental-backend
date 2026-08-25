package com.sunrise.dental.service;

import com.sunrise.dental.dto.request.LoginRequest;
import com.sunrise.dental.dto.request.RegisterUserRequest;
import com.sunrise.dental.dto.response.LoginResponse;
import com.sunrise.dental.dto.response.UserResponse;

/**
 * Authentication and session (JWT cookie) contract.
 */
public interface AuthService {

    LoginResponse login(LoginRequest request, String ipAddress);

    UserResponse registerFirstAdmin(RegisterUserRequest request);

    LoginResponse refreshAccessToken(String refreshToken);

    void logout();

    UserResponse me(String username);
}
