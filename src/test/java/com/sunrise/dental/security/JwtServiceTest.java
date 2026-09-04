package com.sunrise.dental.security;

import com.sunrise.dental.entity.User;
import com.sunrise.dental.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String ACCESS_SECRET =
            "7f3d4a9c1b2e5d6f8a0b1c2d3e4f5a6b7c8d9e0f1a2b3c4d5e6f7a8b9c0d1e2f";
    private static final String REFRESH_SECRET =
            "5c8e6f7a9b0c1d2e3f4a5b6c7d8e9f0a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7";

    private JwtService jwtService;

    private User user() {
        return User.builder()
                .id(1L).username("admin").email("admin@example.com")
                .fullName("Admin User").role(Role.ADMIN)
                .password("p")
                .build();
    }

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(ACCESS_SECRET, REFRESH_SECRET);
    }

    @Nested
    @DisplayName("Access token generation")
    class AccessToken {

        @Test
        @DisplayName("generates a token that extracts the username")
        void generateAndExtract() {
            String token = jwtService.generateAccessToken(user());
            assertNotNull(token);
            assertEquals("admin", jwtService.extractUsername(token));
        }

        @Test
        @DisplayName("extracts role from access token")
        void extractRole() {
            String token = jwtService.generateAccessToken(user());
            assertEquals(Role.ADMIN, jwtService.extractRole(token));
        }

        @Test
        @DisplayName("access token is valid for its user")
        void isValid() {
            String token = jwtService.generateAccessToken(user());
            assertTrue(jwtService.isAccessTokenValid(token, "admin"));
            assertFalse(jwtService.isAccessTokenValid(token, "other"));
        }
    }

    @Nested
    @DisplayName("Refresh token generation")
    class RefreshToken {

        @Test
        @DisplayName("generates a refresh token with independent secret")
        void generateAndExtract() {
            String refresh = jwtService.generateRefreshToken(user());
            assertNotNull(refresh);
            assertEquals("admin", jwtService.extractUsernameFromRefreshToken(refresh));
        }

        @Test
        @DisplayName("refresh token is valid for its user")
        void isValid() {
            String refresh = jwtService.generateRefreshToken(user());
            assertTrue(jwtService.isRefreshTokenValid(refresh, "admin"));
            assertFalse(jwtService.isRefreshTokenValid(refresh, "other"));
        }
    }

    @Nested
    @DisplayName("Cross-use of signing keys")
    class CrossUse {

        @Test
        @DisplayName("refresh token is not accepted as an access token")
        void refreshNotAccess() {
            String refresh = jwtService.generateRefreshToken(user());
            assertThrows(Exception.class, () -> jwtService.isAccessTokenValid(refresh, "admin"));
        }

        @Test
        @DisplayName("access token is not accepted as a refresh token")
        void accessNotRefresh() {
            String access = jwtService.generateAccessToken(user());
            assertThrows(Exception.class, () -> jwtService.isRefreshTokenValid(access, "admin"));
        }
    }

    @Nested
    @DisplayName("Tampered tokens")
    class Tampered {

        @Test
        @DisplayName("an altered token is rejected")
        void tamperedToken() {
            String token = jwtService.generateAccessToken(user());
            String tampered = token.substring(0, token.length() - 2) + "xx";
            assertThrows(Exception.class, () -> jwtService.isAccessTokenValid(tampered, "admin"));
            assertThrows(Exception.class, () -> jwtService.isRefreshTokenValid(tampered, "admin"));
        }
    }
}
