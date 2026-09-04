package com.sunrise.dental.security;

import com.sunrise.dental.constant.SecurityConstants;
import com.sunrise.dental.entity.User;
import com.sunrise.dental.enums.Role;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @BeforeEach
    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private UserDetails userDetails() {
        return org.springframework.security.core.userdetails.User
                .withUsername("admin").password("p")
                .authorities("ROLE_ADMIN").build();
    }

    @Nested
    @DisplayName("No token present")
    class NoToken {

        @Test
        @DisplayName("continues filter chain without authentication")
        void noToken_continues() throws Exception {
            when(request.getHeader(SecurityConstants.AUTHORIZATION_HEADER)).thenReturn(null);
            when(request.getCookies()).thenReturn(null);

            filter.doFilter(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            assertNull(SecurityContextHolder.getContext().getAuthentication());
        }
    }

    @Nested
    @DisplayName("Token in Authorization header")
    class HeaderToken {

        @Test
        @DisplayName("authenticates a valid bearer token")
        void validHeaderToken() throws Exception {
            when(request.getHeader(SecurityConstants.AUTHORIZATION_HEADER))
                    .thenReturn(SecurityConstants.BEARER_PREFIX + "abc.123.tok");
            when(request.getCookies()).thenReturn(null);
            when(jwtService.extractUsername("abc.123.tok")).thenReturn("admin");
            when(userDetailsService.loadUserByUsername("admin")).thenReturn(userDetails());
            when(jwtService.isAccessTokenValid("abc.123.tok", "admin")).thenReturn(true);

            filter.doFilter(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            assertNotNull(SecurityContextHolder.getContext().getAuthentication());
            assertEquals("admin",
                    SecurityContextHolder.getContext().getAuthentication().getName());
        }

        @Test
        @DisplayName("does not authenticate when token invalid")
        void invalidHeaderToken() throws Exception {
            when(request.getHeader(SecurityConstants.AUTHORIZATION_HEADER))
                    .thenReturn(SecurityConstants.BEARER_PREFIX + "abc.123.tok");
            when(request.getCookies()).thenReturn(null);
            when(jwtService.extractUsername("abc.123.tok")).thenReturn("admin");
            when(userDetailsService.loadUserByUsername("admin")).thenReturn(userDetails());
            when(jwtService.isAccessTokenValid("abc.123.tok", "admin")).thenReturn(false);

            filter.doFilter(request, response, filterChain);

            assertNull(SecurityContextHolder.getContext().getAuthentication());
        }

        @Test
        @DisplayName("continues without authentication when username is null")
        void nullUsername() throws Exception {
            when(request.getHeader(SecurityConstants.AUTHORIZATION_HEADER))
                    .thenReturn(SecurityConstants.BEARER_PREFIX + "tok");
            when(request.getCookies()).thenReturn(null);
            when(jwtService.extractUsername("tok")).thenReturn(null);

            filter.doFilter(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            assertNull(SecurityContextHolder.getContext().getAuthentication());
        }
    }

    @Nested
    @DisplayName("Token in cookie")
    class CookieToken {

        @Test
        @DisplayName("authenticates using cookie token")
        void validCookieToken() throws Exception {
            when(request.getHeader(SecurityConstants.AUTHORIZATION_HEADER)).thenReturn(null);
            when(request.getCookies()).thenReturn(
                    new jakarta.servlet.http.Cookie[]{new jakarta.servlet.http.Cookie(
                            SecurityConstants.ACCESS_TOKEN_COOKIE, "cookieToken")});
            when(jwtService.extractUsername("cookieToken")).thenReturn("admin");
            when(userDetailsService.loadUserByUsername("admin")).thenReturn(userDetails());
            when(jwtService.isAccessTokenValid("cookieToken", "admin")).thenReturn(true);

            filter.doFilter(request, response, filterChain);

            assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        }
    }
}
