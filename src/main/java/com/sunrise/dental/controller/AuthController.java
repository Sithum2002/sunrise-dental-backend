package com.sunrise.dental.controller;

import com.sunrise.dental.constant.SecurityConstants;
import com.sunrise.dental.dto.request.LoginRequest;
import com.sunrise.dental.dto.request.RegisterUserRequest;
import com.sunrise.dental.dto.response.ApiResponse;
import com.sunrise.dental.dto.response.LoginResponse;
import com.sunrise.dental.dto.response.UserResponse;
import com.sunrise.dental.security.JwtService;
import com.sunrise.dental.service.AuthService;
import com.sunrise.dental.util.CookieUtil;
import com.sunrise.dental.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication endpoints - issues JWTs in HttpOnly cookies (session-like)
 * and also returns the token in the body for API clients.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Value("${app.cookie.secure:false}")
    private boolean secureCookie;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request,
                                                            HttpServletRequest httpRequest,
                                                            HttpServletResponse response) {
        LoginResponse login = authService.login(request, SecurityUtils.currentIpAddress());
        CookieUtil.addTokenCookies(response, login.getAccessToken(), login.getRefreshToken(), secureCookie);
        return ResponseEntity.ok(ApiResponse.success("Login successful", login));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(HttpServletRequest request,
                                                              HttpServletResponse response) {
        String refreshToken = CookieUtil.getCookie(request, SecurityConstants.REFRESH_TOKEN_COOKIE);
        if (refreshToken == null) {
            refreshToken = request.getHeader("X-Refresh-Token");
        }
        if (refreshToken == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Refresh token missing. Please log in again."));
        }
        LoginResponse login = authService.refreshAccessToken(refreshToken);
        CookieUtil.addTokenCookies(response, login.getAccessToken(), refreshToken, secureCookie);
        return ResponseEntity.ok(ApiResponse.success("Access token refreshed", login));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletResponse response) {
        authService.logout();
        CookieUtil.clearTokenCookies(response);
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> me(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Current user", authService.me(authentication.getName())));
    }

    @PostMapping("/register-first-admin")
    public ResponseEntity<ApiResponse<UserResponse>> registerFirstAdmin(
            @Valid @RequestBody RegisterUserRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Administrator account created", authService.registerFirstAdmin(request)));
    }
}
