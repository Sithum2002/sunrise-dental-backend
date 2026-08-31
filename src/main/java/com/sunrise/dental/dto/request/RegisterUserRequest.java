package com.sunrise.dental.dto.request;

import com.sunrise.dental.constant.RegexPatterns;
import com.sunrise.dental.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterUserRequest {

    @NotBlank(message = "Username is required")
    @Pattern(regexp = RegexPatterns.USERNAME_PATTERN,
            message = "Username must be 3-30 characters (letters, digits, dot, underscore or hyphen)")
    private String username;

    @NotBlank(message = "Password is required")
    @Pattern(regexp = RegexPatterns.PASSWORD_PATTERN,
            message = "Password must be 8-64 characters and contain at least one letter, one digit and one special character")
    private String password;

    @NotBlank(message = "Email is required")
    @Email(message = "A valid email address is required")
    @Size(max = 120, message = "Email must not exceed 120 characters")
    private String email;

    @NotBlank(message = "Full name is required")
    @Size(max = 150, message = "Full name must not exceed 150 characters")
    private String fullName;

    @Pattern(regexp = RegexPatterns.PHONE_GENERAL_PATTERN,
            message = "Contact number must be a valid Sri Lankan number, e.g. +94771234567 or 0771234567")
    @Size(max = 20, message = "Contact number must not exceed 20 characters")
    private String contactNumber;

    @NotNull(message = "Role is required")
    private Role role;
}
