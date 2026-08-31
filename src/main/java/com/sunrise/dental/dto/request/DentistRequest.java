package com.sunrise.dental.dto.request;

import com.sunrise.dental.constant.RegexPatterns;
import com.sunrise.dental.enums.DentistStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DentistRequest {

    @NotBlank(message = "Licence number is required")
    @Pattern(regexp = RegexPatterns.LICENCE_PATTERN, message = "Licence number must follow the format DR-####")
    private String licenceNo;

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;

    @NotBlank(message = "Specialization is required")
    @Size(max = 120, message = "Specialization must not exceed 120 characters")
    private String specialization;

    @NotBlank(message = "Contact number is required")
    @Pattern(regexp = RegexPatterns.PHONE_GENERAL_PATTERN,
            message = "Contact number must be a valid Sri Lankan number, e.g. +94771234567 or 0771234567")
    private String contactNumber;

    @NotBlank(message = "Email is required")
    @Email(message = "A valid email address is required")
    private String email;

    @NotNull(message = "Status is required")
    private DentistStatus status;

    @PositiveOrZero(message = "Years of experience cannot be negative")
    private int yearsOfExperience;

    @PastOrPresent(message = "Joining date cannot be in the future")
    private LocalDate joiningDate;

    @Size(max = 500, message = "Biography must not exceed 500 characters")
    private String biography;
}
