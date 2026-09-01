package com.sunrise.dental.util;

import com.sunrise.dental.constant.SecurityConstants;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;

import java.util.Arrays;

/**
 * Cookie helpers for the token-based session (HttpOnly cookies).
 */
public final class CookieUtil {

    private CookieUtil() {
    }

    public static void addTokenCookies(HttpServletResponse response, String accessToken, String refreshToken,
                                       boolean secure) {
        addCookie(response, SecurityConstants.ACCESS_TOKEN_COOKIE, accessToken,
                (int) (SecurityConstants.ACCESS_TOKEN_VALIDITY_MS / 1000), secure);
        addCookie(response, SecurityConstants.REFRESH_TOKEN_COOKIE, refreshToken,
                (int) (SecurityConstants.REFRESH_TOKEN_VALIDITY_MS / 1000), secure);
    }

    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge, boolean secure) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAge)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    public static void clearTokenCookies(HttpServletResponse response) {
        clearCookie(response, SecurityConstants.ACCESS_TOKEN_COOKIE);
        clearCookie(response, SecurityConstants.REFRESH_TOKEN_COOKIE);
    }

    public static void clearCookie(HttpServletResponse response, String name) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    public static String getCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) {
            return null;
        }
        return Arrays.stream(request.getCookies())
                .filter(c -> name.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
