package com.sunrise.dental.event;

import com.sunrise.dental.entity.Appointment;

import java.time.LocalDateTime;

/**
 * Domain event published when an appointment is cancelled.
 */
public record AppointmentCancelledEvent(Appointment appointment, String reason, LocalDateTime occurredAt) {

    public AppointmentCancelledEvent(Appointment appointment, String reason) {
        this(appointment, reason, LocalDateTime.now());
    }
}
