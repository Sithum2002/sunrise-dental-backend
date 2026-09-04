package com.sunrise.dental.util;

import com.sunrise.dental.constant.SecurityConstants;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CookieUtilTest {

    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        response = mock(HttpServletResponse.class);
    }

    @Test
    @DisplayName("addCookie adds a Set-Cookie header")
    void addCookie() {
        CookieUtil.addCookie(response, "name", "value", 60, true);
        verify(response).addHeader(eq("Set-Cookie"), contains("name=value"));
    }

    @Test
    @DisplayName("addCookie is HttpOnly with Lax same site and root path")
    void addCookie_attributes() {
        CookieUtil.addCookie(response, "name", "value", 60, true);
        verify(response).addHeader(eq("Set-Cookie"), argThat(v ->
                v.contains("HttpOnly") && v.contains("SameSite=Lax") && v.contains("Path=/")
                        && v.contains("Max-Age=60")));
    }

    @Test
    @DisplayName("clearCookie sets max age 0")
    void clearCookie() {
        CookieUtil.clearCookie(response, "name");
        verify(response).addHeader(eq("Set-Cookie"), contains("Max-Age=0"));
    }

    @Test
    @DisplayName("getCookie returns value when present")
    void getCookie_found() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("token", "abc")});

        assertEquals("abc", CookieUtil.getCookie(request, "token"));
    }

    @Test
    @DisplayName("getCookie returns null when cookie not present")
    void getCookie_missing() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("other", "x")});

        assertNull(CookieUtil.getCookie(request, "token"));
    }

    @Test
    @DisplayName("getCookie returns null when no cookies")
    void getCookie_none() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(null);

        assertNull(CookieUtil.getCookie(request, "token"));
    }

    @Test
    @DisplayName("addTokenCookies adds both access and refresh cookies")
    void addTokenCookies() {
        CookieUtil.addTokenCookies(response, "access", "refresh", true);
        verify(response, atLeastOnce()).addHeader(eq("Set-Cookie"),
                argThat(v -> v.contains(SecurityConstants.ACCESS_TOKEN_COOKIE + "=")));
        verify(response, atLeastOnce()).addHeader(eq("Set-Cookie"),
                argThat(v -> v.contains(SecurityConstants.REFRESH_TOKEN_COOKIE + "=")));
    }

    @Test
    @DisplayName("clearTokenCookies clears both cookies")
    void clearTokenCookies() {
        CookieUtil.clearTokenCookies(response);
        verify(response, atLeastOnce()).addHeader(eq("Set-Cookie"),
                argThat(v -> v.contains(SecurityConstants.ACCESS_TOKEN_COOKIE + "=") && v.contains("Max-Age=0")));
        verify(response, atLeastOnce()).addHeader(eq("Set-Cookie"),
                argThat(v -> v.contains(SecurityConstants.REFRESH_TOKEN_COOKIE + "=") && v.contains("Max-Age=0")));
    }
}
