package com.sunrise.dental.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/test");
    }

    private void assertEnvelope(ResponseEntity<ErrorResponse> result, HttpStatus expected) {
        assertEquals(expected, result.getStatusCode());
        assertEquals(expected.value(), result.getBody().getStatus());
        assertEquals("/api/test", result.getBody().getPath());
        assertNotNull(result.getBody().getTimestamp());
    }

    @Nested
    @DisplayName("Custom exceptions")
    class Custom {

        @Test
        @DisplayName("not found maps to 404")
        void notFound() {
            ResponseEntity<ErrorResponse> result =
                    handler.handleNotFound(new ResourceNotFoundException("missing"), request);
            assertEnvelope(result, HttpStatus.NOT_FOUND);
            assertEquals("missing", result.getBody().getMessage());
        }

        @Test
        @DisplayName("duplicate maps to 409")
        void duplicate() {
            ResponseEntity<ErrorResponse> result =
                    handler.handleDuplicate(new DuplicateResourceException("dup"), request);
            assertEnvelope(result, HttpStatus.CONFLICT);
        }

        @Test
        @DisplayName("business rule maps to 400")
        void businessRule() {
            ResponseEntity<ErrorResponse> result =
                    handler.handleBusinessRule(new BusinessRuleException("rule"), request);
            assertEnvelope(result, HttpStatus.BAD_REQUEST);
            assertEquals("rule", result.getBody().getMessage());
        }

        @Test
        @DisplayName("unauthorized access maps to 403")
        void unauthorized() {
            ResponseEntity<ErrorResponse> result =
                    handler.handleUnauthorized(new UnauthorizedAccessException("denied"), request);
            assertEnvelope(result, HttpStatus.FORBIDDEN);
        }
    }

    @Nested
    @DisplayName("Spring Security exceptions")
    class SecurityExceptions {

        @Test
        @DisplayName("bad credentials maps to 401 with generic message")
        void badCredentials() {
            ResponseEntity<ErrorResponse> result =
                    handler.handleBadCredentials(new BadCredentialsException("x"), request);
            assertEnvelope(result, HttpStatus.UNAUTHORIZED);
            assertEquals("Invalid username or password", result.getBody().getMessage());
        }

        @Test
        @DisplayName("disabled account maps to 403")
        void disabled() {
            ResponseEntity<ErrorResponse> result =
                    handler.handleDisabled(new DisabledException("x"), request);
            assertEnvelope(result, HttpStatus.FORBIDDEN);
        }

        @Test
        @DisplayName("locked account maps to 403")
        void locked() {
            ResponseEntity<ErrorResponse> result =
                    handler.handleLocked(new org.springframework.security.authentication.LockedException("x"), request);
            assertEnvelope(result, HttpStatus.FORBIDDEN);
        }

        @Test
        @DisplayName("access denied maps to 403")
        void accessDenied() {
            ResponseEntity<ErrorResponse> result =
                    handler.handleAccessDenied(new AccessDeniedException("x"), request);
            assertEnvelope(result, HttpStatus.FORBIDDEN);
        }
    }

    @Nested
    @DisplayName("Validation exceptions")
    class Validation {

        @Test
        @DisplayName("field validation errors map to 400 with field map")
        void validation() {
            MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
            BindingResult bindingResult = mock(BindingResult.class);
            when(ex.getBindingResult()).thenReturn(bindingResult);
            when(bindingResult.getFieldErrors()).thenReturn(List.of(
                    new FieldError("obj", "email", "must be valid"),
                    new FieldError("obj", "email", "too long")));

            ResponseEntity<ErrorResponse> result = handler.handleValidation(ex, request);

            assertEnvelope(result, HttpStatus.BAD_REQUEST);
            assertEquals(Map.of("email", "must be valid"), result.getBody().getFieldErrors());
        }

        @Test
        @DisplayName("unreadable body maps to 400")
        void unreadable() {
            ResponseEntity<ErrorResponse> result = handler.handleUnreadable(
                    new org.springframework.http.converter.HttpMessageNotReadableException("bad"), request);
            assertEnvelope(result, HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("Generic exceptions")
    class Generic {

        @Test
        @DisplayName("generic exception maps to 500")
        void generic() {
            ResponseEntity<ErrorResponse> result = handler.handleGeneric(new RuntimeException("oops"), request);
            assertEnvelope(result, HttpStatus.INTERNAL_SERVER_ERROR);
            assertEquals("An unexpected error occurred. Please try again later.", result.getBody().getMessage());
        }
    }
}
