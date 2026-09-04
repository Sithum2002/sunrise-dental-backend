package com.sunrise.dental.controller;

import com.sunrise.dental.constant.SecurityConstants;
import com.sunrise.dental.dto.request.LoginRequest;
import com.sunrise.dental.dto.request.RegisterUserRequest;
import com.sunrise.dental.dto.response.ApiResponse;
import com.sunrise.dental.dto.response.LoginResponse;
import com.sunrise.dental.dto.response.UserResponse;
import com.sunrise.dental.enums.Role;
import com.sunrise.dental.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;
    @Mock
    private HttpServletRequest httpServletRequest;
    @Mock
    private HttpServletResponse httpServletResponse;

    @InjectMocks
    private AuthController authController;

    @Nested
    @DisplayName("POST /api/auth/login")
    class Login {

        @Test
        @DisplayName("logs in and adds cookies")
        void login_success() {
            LoginRequest request = LoginRequest.builder()
                    .username("admin").password("Admin@123").build();
            LoginResponse loginResponse = LoginResponse.builder()
                    .accessToken("access-token")
                    .refreshToken("refresh-token")
                    .tokenType("Bearer")
                    .expiresIn(1800)
                    .username("admin")
                    .fullName("Admin")
                    .email("admin@example.com")
                    .role(Role.ADMIN)
                    .loginTime(LocalDateTime.now())
                    .build();
            when(authService.login(eq(request), anyString())).thenReturn(loginResponse);

            ResponseEntity<ApiResponse<LoginResponse>> result =
                    authController.login(request, httpServletRequest, httpServletResponse);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals("Login successful", result.getBody().getMessage());
            assertEquals("access-token", result.getBody().getData().getAccessToken());
            verify(httpServletResponse, atLeastOnce()).addHeader(eq("Set-Cookie"), anyString());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/refresh")
    class Refresh {

        @Test
        @DisplayName("refreshes token from cookie")
        void refresh_fromCookie() {
            Cookie cookie = new Cookie(SecurityConstants.REFRESH_TOKEN_COOKIE, "refresh-token");
            when(httpServletRequest.getCookies()).thenReturn(new Cookie[]{cookie});
            LoginResponse loginResponse = LoginResponse.builder()
                    .accessToken("new-access")
                    .tokenType("Bearer")
                    .expiresIn(1800)
                    .username("admin")
                    .role(Role.ADMIN)
                    .build();
            when(authService.refreshAccessToken("refresh-token")).thenReturn(loginResponse);

            ResponseEntity<ApiResponse<LoginResponse>> result =
                    authController.refresh(httpServletRequest, httpServletResponse);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals("new-access", result.getBody().getData().getAccessToken());
        }

        @Test
        @DisplayName("refreshes token from header when no cookie")
        void refresh_fromHeader() {
            when(httpServletRequest.getCookies()).thenReturn(null);
            when(httpServletRequest.getHeader("X-Refresh-Token")).thenReturn("header-refresh");
            LoginResponse loginResponse = LoginResponse.builder()
                    .accessToken("new-access").tokenType("Bearer").expiresIn(1800)
                    .username("admin").role(Role.ADMIN).build();
            when(authService.refreshAccessToken("header-refresh")).thenReturn(loginResponse);

            ResponseEntity<ApiResponse<LoginResponse>> result =
                    authController.refresh(httpServletRequest, httpServletResponse);

            assertEquals(HttpStatus.OK, result.getStatusCode());
        }

        @Test
        @DisplayName("returns 401 when refresh token missing")
        void refresh_missingToken() {
            when(httpServletRequest.getCookies()).thenReturn(null);
            when(httpServletRequest.getHeader("X-Refresh-Token")).thenReturn(null);

            ResponseEntity<ApiResponse<LoginResponse>> result =
                    authController.refresh(httpServletRequest, httpServletResponse);

            assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
            assertFalse(result.getBody().isSuccess());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/logout")
    class Logout {

        @Test
        @DisplayName("logs out and clears cookies")
        void logout_success() {
            doNothing().when(authService).logout();

            ResponseEntity<ApiResponse<Void>> result = authController.logout(httpServletResponse);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals("Logged out successfully", result.getBody().getMessage());
            verify(authService).logout();
            verify(httpServletResponse, atLeastOnce()).addHeader(eq("Set-Cookie"), anyString());
        }
    }

    @Nested
    @DisplayName("GET /api/auth/me")
    class Me {

        @Test
        @DisplayName("returns current user")
        void me_success() {
            Authentication authentication = mock(Authentication.class);
            when(authentication.getName()).thenReturn("admin");
            UserResponse userResponse = new UserResponse(
                    1L, "admin", "admin@example.com", "Admin",
                    null, Role.ADMIN, true, false, null);
            when(authService.me("admin")).thenReturn(userResponse);

            ResponseEntity<ApiResponse<UserResponse>> result = authController.me(authentication);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals("admin", result.getBody().getData().username());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/register-first-admin")
    class RegisterFirstAdmin {

        @Test
        @DisplayName("registers first admin")
        void registerFirstAdmin_success() {
            RegisterUserRequest request = RegisterUserRequest.builder()
                    .username("admin").password("Admin@123").email("admin@example.com")
                    .fullName("Admin").role(Role.ADMIN).build();
            UserResponse userResponse = new UserResponse(
                    1L, "admin", "admin@example.com", "Admin",
                    null, Role.ADMIN, true, false, null);
            when(authService.registerFirstAdmin(request)).thenReturn(userResponse);

            ResponseEntity<ApiResponse<UserResponse>> result =
                    authController.registerFirstAdmin(request);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals("Administrator account created", result.getBody().getMessage());
            assertEquals("admin", result.getBody().getData().username());
        }
    }
}
