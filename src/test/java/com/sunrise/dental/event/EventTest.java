package com.sunrise.dental.event;

import com.sunrise.dental.entity.Appointment;
import com.sunrise.dental.entity.Bill;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EventTest {

    @Test
    @DisplayName("AppointmentCreatedEvent convenience constructor sets occurredAt")
    void appointmentCreated() {
        Appointment appointment = new Appointment();
        AppointmentCreatedEvent event = new AppointmentCreatedEvent(appointment);

        assertSame(appointment, event.appointment());
        assertNotNull(event.occurredAt());
    }

    @Test
    @DisplayName("AppointmentCancelledEvent captures reason")
    void appointmentCancelled() {
        Appointment appointment = new Appointment();
        AppointmentCancelledEvent event = new AppointmentCancelledEvent(appointment, "patient");

        assertSame(appointment, event.appointment());
        assertEquals("patient", event.reason());
        assertNotNull(event.occurredAt());
    }

    @Test
    @DisplayName("AppointmentCompletedEvent convenience constructor")
    void appointmentCompleted() {
        Appointment appointment = new Appointment();
        AppointmentCompletedEvent event = new AppointmentCompletedEvent(appointment);

        assertSame(appointment, event.appointment());
        assertNotNull(event.occurredAt());
    }

    @Test
    @DisplayName("PaymentReceivedEvent captures amount")
    void paymentReceived() {
        Bill bill = new Bill();
        PaymentReceivedEvent event = new PaymentReceivedEvent(bill, 5000.0);

        assertSame(bill, event.bill());
        assertEquals(5000.0, event.amount());
        assertNotNull(event.occurredAt());
    }

    @Test
    @DisplayName("records with explicit occurredAt retain value")
    void fullConstructor() {
        LocalDateTime ts = LocalDateTime.of(2026, 1, 1, 10, 0);
        AppointmentCreatedEvent event = new AppointmentCreatedEvent(new Appointment(), ts);
        assertEquals(ts, event.occurredAt());
    }
}
