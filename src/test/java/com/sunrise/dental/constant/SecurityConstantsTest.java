package com.sunrise.dental.constant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SecurityConstantsTest {

    @Test
    @DisplayName("cookie names")
    void cookieNames() {
        assertEquals("SD_ACCESS_TOKEN", SecurityConstants.ACCESS_TOKEN_COOKIE);
        assertEquals("SD_REFRESH_TOKEN", SecurityConstants.REFRESH_TOKEN_COOKIE);
    }

    @Test
    @DisplayName("auth header constants")
    void authHeader() {
        assertEquals("Authorization", SecurityConstants.AUTHORIZATION_HEADER);
        assertEquals("Bearer ", SecurityConstants.BEARER_PREFIX);
    }

    @Test
    @DisplayName("token validity durations")
    void validity() {
        assertEquals(30L * 60L * 1000L, SecurityConstants.ACCESS_TOKEN_VALIDITY_MS);
        assertEquals(7L * 24L * 60L * 60L * 1000L, SecurityConstants.REFRESH_TOKEN_VALIDITY_MS);
        assertTrue(SecurityConstants.REFRESH_TOKEN_VALIDITY_MS > SecurityConstants.ACCESS_TOKEN_VALIDITY_MS);
    }
}
