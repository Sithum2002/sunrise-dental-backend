package com.sunrise.dental.controller;

import com.sunrise.dental.dto.request.RegisterUserRequest;
import com.sunrise.dental.dto.response.ApiResponse;
import com.sunrise.dental.dto.response.PageResponse;
import com.sunrise.dental.dto.response.UserResponse;
import com.sunrise.dental.enums.Role;
import com.sunrise.dental.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private UserResponse userResponse() {
        return new UserResponse(
                1L, "admin", "admin@example.com", "Admin",
                "0771234567", Role.ADMIN, true, false, null);
    }

    private RegisterUserRequest userRequest() {
        return RegisterUserRequest.builder()
                .username("admin").password("Admin@123").email("admin@example.com")
                .fullName("Admin").contactNumber("0771234567").role(Role.ADMIN)
                .build();
    }

    @Nested
    @DisplayName("GET /api/admin/users")
    class GetAll {

        @Test
        @DisplayName("returns paginated users")
        void getAll_success() {
            PageResponse<UserResponse> pageResponse = new PageResponse<>(
                    List.of(userResponse()), 0, 10, 1, 1);
            Pageable pageable = PageRequest.of(0, 10);
            when(userService.getAll(pageable)).thenReturn(pageResponse);

            ResponseEntity<ApiResponse<PageResponse<UserResponse>>> result =
                    userController.getAll(pageable);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals(1, result.getBody().getData().content().size());
        }

        @Test
        @DisplayName("returns empty page when no users")
        void getAll_empty() {
            PageResponse<UserResponse> pageResponse = new PageResponse<>(
                    Collections.emptyList(), 0, 10, 0, 0);
            when(userService.getAll(any(Pageable.class))).thenReturn(pageResponse);

            ResponseEntity<ApiResponse<PageResponse<UserResponse>>> result =
                    userController.getAll(PageRequest.of(0, 10));

            assertTrue(result.getBody().getData().content().isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /api/admin/users/{id}")
    class GetById {

        @Test
        @DisplayName("returns user by id")
        void getById_success() {
            when(userService.getById(1L)).thenReturn(userResponse());

            ResponseEntity<ApiResponse<UserResponse>> result = userController.getById(1L);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals("admin", result.getBody().getData().username());
        }
    }

    @Nested
    @DisplayName("POST /api/admin/users")
    class Create {

        @Test
        @DisplayName("creates user")
        void create_success() {
            RegisterUserRequest request = userRequest();
            when(userService.create(request)).thenReturn(userResponse());

            ResponseEntity<ApiResponse<UserResponse>> result = userController.create(request);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals("User created", result.getBody().getMessage());
            verify(userService).create(request);
        }
    }

    @Nested
    @DisplayName("PUT /api/admin/users/{id}")
    class Update {

        @Test
        @DisplayName("updates user")
        void update_success() {
            RegisterUserRequest request = userRequest();
            when(userService.update(1L, request)).thenReturn(userResponse());

            ResponseEntity<ApiResponse<UserResponse>> result = userController.update(1L, request);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals("User updated", result.getBody().getMessage());
            verify(userService).update(1L, request);
        }
    }

    @Nested
    @DisplayName("DELETE /api/admin/users/{id}")
    class Delete {

        @Test
        @DisplayName("deletes user")
        void delete_success() {
            doNothing().when(userService).delete(1L);

            ResponseEntity<ApiResponse<Void>> result = userController.delete(1L);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals("User deactivated", result.getBody().getMessage());
            verify(userService).delete(1L);
        }
    }

    @Nested
    @DisplayName("PATCH /api/admin/users/{id}/toggle-active")
    class ToggleActive {

        @Test
        @DisplayName("toggles active state")
        void toggleActive_success() {
            doNothing().when(userService).toggleActive(1L);

            ResponseEntity<ApiResponse<Void>> result = userController.toggleActive(1L);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            verify(userService).toggleActive(1L);
        }
    }

    @Nested
    @DisplayName("PATCH /api/admin/users/{id}/unlock")
    class Unlock {

        @Test
        @DisplayName("unlocks user")
        void unlock_success() {
            doNothing().when(userService).resetFailedAttempts(1L);

            ResponseEntity<ApiResponse<Void>> result = userController.unlock(1L);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals("User account unlocked", result.getBody().getMessage());
            verify(userService).resetFailedAttempts(1L);
        }
    }
}
