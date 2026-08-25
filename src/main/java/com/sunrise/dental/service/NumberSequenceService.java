package com.sunrise.dental.service;

/**
 * Generates sequential business numbers (patient reg no, appointment no,
 * bill no, user code) in a thread-safe, database-backed way.
 */
public interface NumberSequenceService {

    String nextPatientRegNo();

    String nextAppointmentNumber();

    String nextBillNumber();
}
