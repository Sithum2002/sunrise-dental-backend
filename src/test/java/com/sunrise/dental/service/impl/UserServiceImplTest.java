package com.sunrise.dental.service.impl;

import com.sunrise.dental.audit.AuditService;
import com.sunrise.dental.dto.request.RegisterUserRequest;
import com.sunrise.dental.dto.response.PageResponse;
import com.sunrise.dental.dto.response.UserResponse;
import com.sunrise.dental.entity.User;
import com.sunrise.dental.enums.Role;
import com.sunrise.dental.exception.BusinessRuleException;
import com.sunrise.dental.exception.DuplicateResourceException;
import com.sunrise.dental.exception.ResourceNotFoundException;
import com.sunrise.dental.mapper.UserMapper;
import com.sunrise.dental.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;
    @Mock
    private AuditService auditService;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("admin")
                .password("encoded")
                .email("admin@example.com")
                .fullName("System Admin")
                .contactNumber("0771234567")
                .role(Role.ADMIN)
                .active(true)
                .build();

        userResponse = new UserResponse(1L, "admin", "admin@example.com",
                "System Admin", "0771234567", Role.ADMIN, true, false, null);
    }

    private RegisterUserRequest validRequest() {
        return RegisterUserRequest.builder()
                .username("admin")
                .password("Admin@123")
                .email("admin@example.com")
                .fullName("System Admin")
                .contactNumber("0771234567")
                .role(Role.ADMIN)
                .build();
    }

    @Nested
    @DisplayName("getAll()")
    class GetAll {

        @Test
        @DisplayName("returns paginated users")
        void getAll_success() {
            Page<User> page = new PageImpl<>(List.of(user));
            Pageable pageable = PageRequest.of(0, 10);
            when(userRepository.findAll(pageable)).thenReturn(page);
            when(userMapper.toResponse(user)).thenReturn(userResponse);

            PageResponse<UserResponse> result = userService.getAll(pageable);

            assertNotNull(result);
            assertEquals(1, result.content().size());
            assertEquals("admin", result.content().get(0).username());
        }

        @Test
        @DisplayName("returns empty page when no users")
        void getAll_empty() {
            Pageable pageable = PageRequest.of(0, 10);
            when(userRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of()));

            PageResponse<UserResponse> result = userService.getAll(pageable);

            assertTrue(result.content().isEmpty());
        }
    }

    @Nested
    @DisplayName("getById()")
    class GetById {

        @Test
        @DisplayName("returns user by id")
        void getById_success() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(userMapper.toResponse(user)).thenReturn(userResponse);

            UserResponse result = userService.getById(1L);

            assertEquals("admin", result.username());
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when user not found")
        void getById_notFound() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> userService.getById(99L));
        }
    }

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("creates user successfully with encoded password")
        void create_success() {
            when(userRepository.existsByUsername("admin")).thenReturn(false);
            when(userRepository.existsByEmail("admin@example.com")).thenReturn(false);
            when(passwordEncoder.encode("Admin@123")).thenReturn("encoded");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User saved = inv.getArgument(0);
                saved.setId(1L);
                return saved;
            });
            when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);

            UserResponse result = userService.create(validRequest());

            assertNotNull(result);
            verify(passwordEncoder).encode("Admin@123");
            verify(userRepository).save(argThat(u -> u.isActive() && "encoded".equals(u.getPassword())));
            verify(auditService).log(eq("CREATE"), eq("User"), eq(1L), anyString());
        }

        @Test
        @DisplayName("throws DuplicateResourceException when username exists")
        void create_duplicateUsername() {
            when(userRepository.existsByUsername("admin")).thenReturn(true);

            assertThrows(DuplicateResourceException.class, () -> userService.create(validRequest()));
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws DuplicateResourceException when email exists")
        void create_duplicateEmail() {
            when(userRepository.existsByUsername("admin")).thenReturn(false);
            when(userRepository.existsByEmail("admin@example.com")).thenReturn(true);

            assertThrows(DuplicateResourceException.class, () -> userService.create(validRequest()));
        }
    }

    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("updates user details")
        void update_success() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(userMapper.toResponse(user)).thenReturn(userResponse);

            RegisterUserRequest request = validRequest();
            request.setPassword("__unchanged__");

            userService.update(1L, request);

            verify(userRepository).save(user);
            verify(userRepository, never()).existsByEmail(anyString());
            verify(auditService).log(eq("UPDATE"), eq("User"), eq(1L), anyString());
        }

        @Test
        @DisplayName("re-encodes password when a new one is provided")
        void update_newPassword() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(passwordEncoder.encode("NewPass@123")).thenReturn("new-encoded");
            when(userMapper.toResponse(user)).thenReturn(userResponse);

            RegisterUserRequest request = validRequest();
            request.setPassword("NewPass@123");

            userService.update(1L, request);

            assertEquals("new-encoded", user.getPassword());
        }

        @Test
        @DisplayName("throws DuplicateResourceException when email owned by another")
        void update_duplicateEmail() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(userRepository.existsByEmail("other@example.com")).thenReturn(true);

            RegisterUserRequest request = validRequest();
            request.setEmail("other@example.com");

            assertThrows(DuplicateResourceException.class, () -> userService.update(1L, request));
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when user not found")
        void update_notFound() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> userService.update(99L, validRequest()));
        }
    }

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("soft-deletes a user")
        void delete_success() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(userRepository.countByActiveTrue()).thenReturn(3L);
            when(userRepository.save(any(User.class))).thenReturn(user);

            userService.delete(1L);

            assertFalse(user.isActive());
            verify(auditService).log(eq("DELETE"), eq("User"), eq(1L), anyString());
        }

        @Test
        @DisplayName("throws BusinessRuleException when deleting last active admin")
        void delete_lastAdmin() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(userRepository.countByActiveTrue()).thenReturn(1L);

            BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                    () -> userService.delete(1L));
            assertTrue(ex.getMessage().contains("last active administrator"));
        }

        @Test
        @DisplayName("allows deleting non-admin when only one active")
        void delete_nonAdminOnlyActive() {
            user.setRole(Role.RECEPTIONIST);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenReturn(user);

            userService.delete(1L);

            assertFalse(user.isActive());
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when user not found")
        void delete_notFound() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> userService.delete(99L));
        }
    }

    @Nested
    @DisplayName("toggleActive()")
    class ToggleActive {

        @Test
        @DisplayName("toggles user active state")
        void toggleActive_success() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(userRepository.countByActiveTrue()).thenReturn(2L);
            when(userRepository.save(any(User.class))).thenReturn(user);

            userService.toggleActive(1L);

            assertFalse(user.isActive());
            verify(auditService).log(eq("TOGGLE_ACTIVE"), eq("User"), eq(1L), anyString());
        }

        @Test
        @DisplayName("throws BusinessRuleException when deactivating last active admin")
        void toggleActive_lastAdmin() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(userRepository.countByActiveTrue()).thenReturn(1L);

            assertThrows(BusinessRuleException.class, () -> userService.toggleActive(1L));
        }

        @Test
        @DisplayName("allows reactivating an inactive admin")
        void toggleActive_reactivate() {
            user.setActive(false);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenReturn(user);

            userService.toggleActive(1L);

            assertTrue(user.isActive());
            verify(userRepository, never()).countByActiveTrue();
        }
    }

    @Nested
    @DisplayName("resetFailedAttempts()")
    class ResetFailedAttempts {

        @Test
        @DisplayName("resets failed attempts and unlocks account")
        void resetFailedAttempts_success() {
            user.setFailedAttempts(5);
            user.setAccountLocked(true);
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenReturn(user);

            userService.resetFailedAttempts(1L);

            assertEquals(0, user.getFailedAttempts());
            assertFalse(user.isAccountLocked());
            verify(auditService).log(eq("UNLOCK"), eq("User"), eq(1L), anyString());
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when user not found")
        void resetFailedAttempts_notFound() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> userService.resetFailedAttempts(99L));
        }
    }
}
