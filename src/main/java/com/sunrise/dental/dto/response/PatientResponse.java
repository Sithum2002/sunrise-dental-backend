package com.sunrise.dental.dto.response;

import com.sunrise.dental.enums.BloodGroup;
import com.sunrise.dental.enums.Gender;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PatientResponse(
        Long id,
        String regNo,
        String firstName,
        String lastName,
        String fullName,
        String address,
        String contactNumber,
        String email,
        LocalDate dateOfBirth,
        Gender gender,
        BloodGroup bloodGroup,
        String allergies,
        String medicalHistory,
        String emergencyContact,
        boolean active,
        LocalDateTime createdDate
) {
}
