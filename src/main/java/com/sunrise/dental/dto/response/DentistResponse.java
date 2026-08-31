package com.sunrise.dental.dto.response;

import com.sunrise.dental.enums.DentistStatus;

import java.time.LocalDate;

public record DentistResponse(
        Long id,
        String licenceNo,
        String firstName,
        String lastName,
        String fullName,
        String specialization,
        String contactNumber,
        String email,
        DentistStatus status,
        int yearsOfExperience,
        LocalDate joiningDate,
        String biography
) {
}
