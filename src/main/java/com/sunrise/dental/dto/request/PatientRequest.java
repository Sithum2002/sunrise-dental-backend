package com.sunrise.dental.dto.request;

import com.sunrise.dental.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.sunrise.dental.constant.RegexPatterns;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    @Pattern(regexp = "^[A-Za-z\\s.'-]+$", message = "First name may contain letters, spaces, dot, apostrophe and hyphen only")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    @Pattern(regexp = "^[A-Za-z\\s.'-]+$", message = "Last name may contain letters, spaces, dot, apostrophe and hyphen only")
    private String lastName;

    @NotBlank(message = "Address is required")
    @Size(max = 250, message = "Address must not exceed 250 characters")
    private String address;

    @NotBlank(message = "Contact number is required")
    @Pattern(regexp = RegexPatterns.PHONE_GENERAL_PATTERN,
            message = "Contact number must be a valid Sri Lankan number, e.g. +94771234567 or 0771234567")
    private String contactNumber;

    @NotBlank(message = "Email is required")
    @Email(message = "A valid email address is required")
    @Size(max = 120, message = "Email must not exceed 120 characters")
    private String email;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotNull(message = "Gender is required")
    private Gender gender;

    private com.sunrise.dental.enums.BloodGroup bloodGroup;

    @Size(max = 500, message = "Allergies must not exceed 500 characters")
    private String allergies;

    @Size(max = 1000, message = "Medical history must not exceed 1000 characters")
    private String medicalHistory;

    @Pattern(regexp = RegexPatterns.PHONE_GENERAL_PATTERN,
            message = "Emergency contact must be a valid Sri Lankan number, e.g. +94771234567 or 0771234567")
    private String emergencyContact;
}
