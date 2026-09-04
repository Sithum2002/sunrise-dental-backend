package com.sunrise.dental.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SecurityUtilsTest {

    @BeforeEach
    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("currentUsername returns system when no auth")
    void currentUsername_noAuth() {
        assertEquals("system", SecurityUtils.currentUsername());
    }

    @Test
    @DisplayName("currentUsername returns principal name when authenticated")
    void currentUsername_authenticated() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("doctor", "pw", List.of()));
        assertEquals("doctor", SecurityUtils.currentUsername());
    }

    @Test
    @DisplayName("isAuthenticated returns false when no auth")
    void isAuthenticated_noAuth() {
        assertFalse(SecurityUtils.isAuthenticated());
    }

    @Test
    @DisplayName("isAuthenticated returns false for anonymous user")
    void isAuthenticated_anonymous() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("anonymousUser", "pw"));
        assertFalse(SecurityUtils.isAuthenticated());
    }

    @Test
    @DisplayName("isAuthenticated returns true when authenticated")
    void isAuthenticated_authenticated() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("doctor", "pw", List.of()));
        assertTrue(SecurityUtils.isAuthenticated());
    }

    @Test
    @DisplayName("currentIpAddress returns localhost without request context")
    void currentIpAddress_noRequest() {
        assertEquals("localhost", SecurityUtils.currentIpAddress());
    }
}
