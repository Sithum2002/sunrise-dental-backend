package com.sunrise.dental.event;

import com.sunrise.dental.entity.Appointment;

import java.time.LocalDateTime;

/**
 * Domain event published when an appointment is completed.
 */
public record AppointmentCompletedEvent(Appointment appointment, LocalDateTime occurredAt) {

    public AppointmentCompletedEvent(Appointment appointment) {
        this(appointment, LocalDateTime.now());
    }
}
