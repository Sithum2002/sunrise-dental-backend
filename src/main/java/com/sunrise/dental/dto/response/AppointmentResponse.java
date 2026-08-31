package com.sunrise.dental.dto.response;

import com.sunrise.dental.enums.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record AppointmentResponse(
        Long id,
        String appointmentNumber,
        Long patientId,
        String patientName,
        String patientContact,
        Long dentistId,
        String dentistName,
        Long treatmentId,
        String treatmentName,
        String treatmentCode,
        LocalDate appointmentDate,
        LocalTime startTime,
        LocalTime endTime,
        AppointmentStatus status,
        String notes,
        String completedNotes,
        LocalDateTime createdDate
) {
}
