package com.sunrise.dental.dto.response;

import com.sunrise.dental.enums.Role;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String username,
        String email,
        String fullName,
        String contactNumber,
        Role role,
        boolean active,
        boolean accountLocked,
        LocalDateTime lastLoginDate
) {
}
