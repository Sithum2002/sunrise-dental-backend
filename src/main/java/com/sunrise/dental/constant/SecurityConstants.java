package com.sunrise.dental.constant;

/**
 * Security related constants (cookies, token headers, durations).
 */
public final class SecurityConstants {

    private SecurityConstants() {
    }

    /** HttpOnly cookie that carries the access token. */
    public static final String ACCESS_TOKEN_COOKIE = "SD_ACCESS_TOKEN";

    /** HttpOnly cookie that carries the refresh token. */
    public static final String REFRESH_TOKEN_COOKIE = "SD_REFRESH_TOKEN";

    /** Standard authorization header (also accepted). */
    public static final String AUTHORIZATION_HEADER = "Authorization";

    /** Bearer scheme prefix. */
    public static final String BEARER_PREFIX = "Bearer ";

    /** Access token lifetime in milliseconds (30 minutes). */
    public static final long ACCESS_TOKEN_VALIDITY_MS = 30L * 60L * 1000L;

    /** Refresh token lifetime in milliseconds (7 days). */
    public static final long REFRESH_TOKEN_VALIDITY_MS = 7L * 24L * 60L * 60L * 1000L;
}
