package com.sunrise.dental.event;

import com.sunrise.dental.entity.Appointment;

import java.time.LocalDateTime;

/**
 * Domain event published when a new appointment is registered.
 * The NotificationListener (observer) reacts by sending email/SMS.
 */
public record AppointmentCreatedEvent(Appointment appointment, LocalDateTime occurredAt) {

    public AppointmentCreatedEvent(Appointment appointment) {
        this(appointment, LocalDateTime.now());
    }
}
