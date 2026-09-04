package com.sunrise.dental.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class AppointmentTimeValidatorTest {

    private AppointmentTimeValidator validator;

    @BeforeEach
    void setUp() {
        validator = new AppointmentTimeValidator();
    }

    @Nested
    @DisplayName("isValid")
    class IsValid {

        @Test
        @DisplayName("passes null (no time provided)")
        void nullIsValid() {
            assertTrue(validator.isValid(null, null));
        }

        @Test
        @DisplayName("accepts opening time")
        void opening() {
            assertTrue(validator.isValid(LocalTime.of(8, 0), null));
        }

        @Test
        @DisplayName("accepts last allowed slot")
        void lastSlot() {
            assertTrue(validator.isValid(LocalTime.of(17, 30), null));
        }

        @Test
        @DisplayName("accepts mid-morning slot")
        void morning() {
            assertTrue(validator.isValid(LocalTime.of(10, 30), null));
        }

        @Test
        @DisplayName("accepts afternoon slot after lunch")
        void afternoon() {
            assertTrue(validator.isValid(LocalTime.of(14, 0), null));
        }

        @Test
        @DisplayName("rejects time before opening")
        void beforeOpening() {
            assertFalse(validator.isValid(LocalTime.of(7, 59), null));
        }

        @Test
        @DisplayName("rejects time after last slot")
        void afterLastSlot() {
            assertFalse(validator.isValid(LocalTime.of(17, 31), null));
        }

        @Test
        @DisplayName("rejects lunch break time")
        void lunch() {
            assertFalse(validator.isValid(LocalTime.of(13, 0), null));
        }

        @Test
        @DisplayName("rejects exact lunch boundaries and accepts just after")
        void lunchBoundaries() {
            assertFalse(validator.isValid(LocalTime.of(12, 30), null));
            assertFalse(validator.isValid(LocalTime.of(13, 30), null));
            assertTrue(validator.isValid(LocalTime.of(13, 31), null));
        }
    }
}
