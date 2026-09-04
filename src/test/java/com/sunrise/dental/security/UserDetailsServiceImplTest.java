package com.sunrise.dental.security;

import com.sunrise.dental.entity.User;
import com.sunrise.dental.enums.Role;
import com.sunrise.dental.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private User user() {
        return User.builder()
                .username("admin").password("p").email("a@a.com")
                .fullName("Admin").role(Role.ADMIN)
                .active(true).accountLocked(false)
                .build();
    }

    @Nested
    @DisplayName("loadUserByUsername")
    class LoadByUsername {

        @Test
        @DisplayName("loads an active user with role authority")
        void success() {
            when(userRepository.findByUsernameOrEmail("admin")).thenReturn(Optional.of(user()));

            UserDetails details = userDetailsService.loadUserByUsername("admin");

            assertEquals("admin", details.getUsername());
            assertEquals("p", details.getPassword());
            assertTrue(details.isEnabled());
            assertTrue(details.isAccountNonLocked());
            assertTrue(details.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
            verify(userRepository).findByUsernameOrEmail("admin");
        }

        @Test
        @DisplayName("disables inactive user")
        void inactiveUser() {
            User inactive = user();
            inactive.setActive(false);
            when(userRepository.findByUsernameOrEmail("admin")).thenReturn(Optional.of(inactive));

            UserDetails details = userDetailsService.loadUserByUsername("admin");

            assertFalse(details.isEnabled());
        }

        @Test
        @DisplayName("locks a locked user account")
        void lockedUser() {
            User locked = user();
            locked.setAccountLocked(true);
            when(userRepository.findByUsernameOrEmail("admin")).thenReturn(Optional.of(locked));

            UserDetails details = userDetailsService.loadUserByUsername("admin");

            assertFalse(details.isAccountNonLocked());
        }

        @Test
        @DisplayName("throws when user not found")
        void notFound() {
            when(userRepository.findByUsernameOrEmail("ghost")).thenReturn(Optional.empty());

            assertThrows(UsernameNotFoundException.class,
                    () -> userDetailsService.loadUserByUsername("ghost"));
        }
    }
}
