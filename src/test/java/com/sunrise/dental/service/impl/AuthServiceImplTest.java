package com.sunrise.dental.service.impl;

import com.sunrise.dental.audit.AuditService;
import com.sunrise.dental.constant.SecurityConstants;
import com.sunrise.dental.dto.request.LoginRequest;
import com.sunrise.dental.dto.request.RegisterUserRequest;
import com.sunrise.dental.dto.response.LoginResponse;
import com.sunrise.dental.dto.response.UserResponse;
import com.sunrise.dental.entity.User;
import com.sunrise.dental.enums.Role;
import com.sunrise.dental.exception.BusinessRuleException;
import com.sunrise.dental.exception.DuplicateResourceException;
import com.sunrise.dental.exception.ResourceNotFoundException;
import com.sunrise.dental.mapper.UserMapper;
import com.sunrise.dental.repository.UserRepository;
import com.sunrise.dental.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;
    @Mock
    private AuditService auditService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("admin")
                .password("encoded-password")
                .email("admin@example.com")
                .fullName("System Admin")
                .role(Role.ADMIN)
                .active(true)
                .build();

        userResponse = new UserResponse(1L, "admin", "admin@example.com",
                "System Admin", "0771234567", Role.ADMIN, true, false, null);
    }

    @Nested
    @DisplayName("login()")
    class Login {

        @Test
        @DisplayName("logs in successfully and returns tokens")
        void login_success() {
            Authentication authentication = mock(Authentication.class);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(userRepository.findByUsernameOrEmail("admin")).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(jwtService.generateAccessToken(user)).thenReturn("access-token");
            when(jwtService.generateRefreshToken(user)).thenReturn("refresh-token");

            LoginResponse result = authService.login(
                    LoginRequest.builder().username("admin").password("Admin@123").build(),
                    "127.0.0.1");

            assertNotNull(result);
            assertEquals("access-token", result.getAccessToken());
            assertEquals("refresh-token", result.getRefreshToken());
            assertEquals("Bearer", result.getTokenType());
            assertEquals("admin", result.getUsername());
            assertEquals(Role.ADMIN, result.getRole());
            assertEquals(SecurityConstants.ACCESS_TOKEN_VALIDITY_MS / 1000, result.getExpiresIn());
            assertEquals(0, user.getFailedAttempts());
            assertNotNull(user.getLastLoginDate());
            verify(auditService).log(eq("LOGIN"), eq("User"), eq(1L), anyString());
        }

        @Test
        @DisplayName("resets failed attempts on successful login")
        void login_resetsFailedAttempts() {
            user.setFailedAttempts(5);
            Authentication authentication = mock(Authentication.class);
            when(authenticationManager.authenticate(any())).thenReturn(authentication);
            when(userRepository.findByUsernameOrEmail("admin")).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(jwtService.generateAccessToken(user)).thenReturn("t");
            when(jwtService.generateRefreshToken(user)).thenReturn("r");

            authService.login(LoginRequest.builder().username("admin").password("Admin@123").build(),
                    "127.0.0.1");

            assertEquals(0, user.getFailedAttempts());
        }

        @Test
        @DisplayName("throws BusinessRuleException when user no longer exists after auth")
        void login_userNotFound() {
            Authentication authentication = mock(Authentication.class);
            when(authenticationManager.authenticate(any())).thenReturn(authentication);
            when(userRepository.findByUsernameOrEmail("admin")).thenReturn(Optional.empty());

            assertThrows(BusinessRuleException.class,
                    () -> authService.login(LoginRequest.builder().username("admin")
                            .password("Admin@123").build(), "localhost"));
        }
    }

    @Nested
    @DisplayName("registerFirstAdmin()")
    class RegisterFirstAdmin {

        private RegisterUserRequest registerRequest() {
            return RegisterUserRequest.builder()
                    .username("newadmin")
                    .password("Admin@123")
                    .email("newadmin@example.com")
                    .fullName("New Admin")
                    .contactNumber("0771234567")
                    .role(Role.ADMIN)
                    .build();
        }

        @Test
        @DisplayName("registers first admin on a fresh system")
        void registerFirstAdmin_success() {
            when(userRepository.count()).thenReturn(0L);
            when(userRepository.existsByUsername("newadmin")).thenReturn(false);
            when(userRepository.existsByEmail("newadmin@example.com")).thenReturn(false);
            when(passwordEncoder.encode("Admin@123")).thenReturn("encoded");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User saved = inv.getArgument(0);
                saved.setId(1L);
                return saved;
            });
            when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);

            UserResponse result = authService.registerFirstAdmin(registerRequest());

            assertNotNull(result);
            verify(auditService).log(eq("REGISTER"), eq("User"), eq(1L), anyString());
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("throws BusinessRuleException when system is not fresh")
        void registerFirstAdmin_notFresh() {
            when(userRepository.count()).thenReturn(2L);

            BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                    () -> authService.registerFirstAdmin(registerRequest()));
            assertTrue(ex.getMessage().contains("fresh system"));
        }

        @Test
        @DisplayName("throws DuplicateResourceException when username taken")
        void registerFirstAdmin_usernameTaken() {
            when(userRepository.count()).thenReturn(0L);
            when(userRepository.existsByUsername("newadmin")).thenReturn(true);

            assertThrows(DuplicateResourceException.class,
                    () -> authService.registerFirstAdmin(registerRequest()));
        }

        @Test
        @DisplayName("throws DuplicateResourceException when email taken")
        void registerFirstAdmin_emailTaken() {
            when(userRepository.count()).thenReturn(0L);
            when(userRepository.existsByUsername("newadmin")).thenReturn(false);
            when(userRepository.existsByEmail("newadmin@example.com")).thenReturn(true);

            assertThrows(DuplicateResourceException.class,
                    () -> authService.registerFirstAdmin(registerRequest()));
        }

        @Test
        @DisplayName("throws BusinessRuleException when role is not ADMIN")
        void registerFirstAdmin_wrongRole() {
            RegisterUserRequest request = registerRequest();
            request.setRole(Role.DOCTOR);
            when(userRepository.count()).thenReturn(0L);
            when(userRepository.existsByUsername("newadmin")).thenReturn(false);
            when(userRepository.existsByEmail("newadmin@example.com")).thenReturn(false);

            assertThrows(BusinessRuleException.class, () -> authService.registerFirstAdmin(request));
        }

        @Test
        @DisplayName("encodes the password before persistence")
        void registerFirstAdmin_encodesPassword() {
            when(userRepository.count()).thenReturn(0L);
            when(userRepository.existsByUsername("newadmin")).thenReturn(false);
            when(userRepository.existsByEmail("newadmin@example.com")).thenReturn(false);
            when(passwordEncoder.encode("Admin@123")).thenReturn("hashed-secret");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            when(userMapper.toResponse(any(User.class))).thenReturn(userResponse);

            authService.registerFirstAdmin(registerRequest());

            verify(passwordEncoder).encode("Admin@123");
            verify(userRepository).save(argThat(u -> "hashed-secret".equals(u.getPassword())
                    && u.getRole() == Role.ADMIN && u.isActive()));
        }
    }

    @Nested
    @DisplayName("refreshAccessToken()")
    class Refresh {

        @Test
        @DisplayName("refreshes access token successfully")
        void refresh_success() {
            when(jwtService.extractUsernameFromRefreshToken("refresh-token")).thenReturn("admin");
            when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
            when(jwtService.isRefreshTokenValid("refresh-token", "admin")).thenReturn(true);
            when(jwtService.generateAccessToken(user)).thenReturn("new-access-token");

            LoginResponse result = authService.refreshAccessToken("refresh-token");

            assertNotNull(result);
            assertEquals("new-access-token", result.getAccessToken());
            assertEquals("admin", result.getUsername());
            assertNull(result.getRefreshToken());
        }

        @Test
        @DisplayName("throws BusinessRuleException when user does not exist")
        void refresh_userNotFound() {
            when(jwtService.extractUsernameFromRefreshToken("refresh-token")).thenReturn("ghost");
            when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

            assertThrows(BusinessRuleException.class, () -> authService.refreshAccessToken("refresh-token"));
        }

        @Test
        @DisplayName("throws BusinessRuleException when refresh token invalid")
        void refresh_invalidToken() {
            when(jwtService.extractUsernameFromRefreshToken("bad-token")).thenReturn("admin");
            when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
            when(jwtService.isRefreshTokenValid("bad-token", "admin")).thenReturn(false);

            assertThrows(BusinessRuleException.class, () -> authService.refreshAccessToken("bad-token"));
        }
    }

    @Nested
    @DisplayName("logout()")
    class Logout {

        @Test
        @DisplayName("logs audit entry")
        void logout() {
            authService.logout();
            verify(auditService).log(eq("LOGOUT"), eq("User"), isNull(), anyString());
        }
    }

    @Nested
    @DisplayName("me()")
    class Me {

        @Test
        @DisplayName("returns current user by username")
        void me_success() {
            when(userRepository.findByUsernameOrEmail("admin")).thenReturn(Optional.of(user));
            when(userMapper.toResponse(user)).thenReturn(userResponse);

            UserResponse result = authService.me("admin");

            assertNotNull(result);
            assertEquals("admin", result.username());
        }

        @Test
        @DisplayName("returns current user by email")
        void me_byEmail() {
            when(userRepository.findByUsernameOrEmail("admin@example.com")).thenReturn(Optional.of(user));
            when(userMapper.toResponse(user)).thenReturn(userResponse);

            UserResponse result = authService.me("admin@example.com");

            assertEquals("admin", result.username());
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when user not found")
        void me_notFound() {
            when(userRepository.findByUsernameOrEmail("ghost")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> authService.me("ghost"));
        }
    }
}
