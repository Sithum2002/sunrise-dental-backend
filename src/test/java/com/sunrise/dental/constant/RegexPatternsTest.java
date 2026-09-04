package com.sunrise.dental.constant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.*;

class RegexPatternsTest {

    @Nested
    @DisplayName("Phone patterns")
    class Phone {

        @Test
        @DisplayName("matches Sri Lankan mobile formats")
        void mobile() {
            assertTrue("0771234567".matches(RegexPatterns.PHONE_PATTERN));
            assertTrue("0712345678".matches(RegexPatterns.PHONE_PATTERN));
            assertFalse("0123456789".matches(RegexPatterns.PHONE_PATTERN));
        }

        @Test
        @DisplayName("matches general phone")
        void general() {
            assertTrue("0112345678".matches(RegexPatterns.PHONE_GENERAL_PATTERN));
            assertTrue("+94112345678".matches(RegexPatterns.PHONE_GENERAL_PATTERN));
        }
    }

    @Nested
    @DisplayName("Business number patterns")
    class Numbers {

        @Test
        @DisplayName("appointment number")
        void appointmentNumber() {
            assertTrue("AP-2026-0001".matches(RegexPatterns.APPOINTMENT_NUMBER_PATTERN));
            assertFalse("AP-0001".matches(RegexPatterns.APPOINTMENT_NUMBER_PATTERN));
        }

        @Test
        @DisplayName("patient reg number")
        void patientReg() {
            assertTrue("SD-P0001".matches(RegexPatterns.PATIENT_REG_NUMBER_PATTERN));
            assertFalse("P-0001".matches(RegexPatterns.PATIENT_REG_NUMBER_PATTERN));
        }

        @Test
        @DisplayName("bill number")
        void billNumber() {
            assertTrue("INV-0001".matches(RegexPatterns.BILL_NUMBER_PATTERN));
            assertFalse("INV0001".matches(RegexPatterns.BILL_NUMBER_PATTERN));
        }

        @Test
        @DisplayName("licence number")
        void licence() {
            assertTrue("DR-0001".matches(RegexPatterns.LICENCE_PATTERN));
            assertFalse("DR0001".matches(RegexPatterns.LICENCE_PATTERN));
        }
    }

    @Nested
    @DisplayName("Authentication patterns")
    class Auth {

        @Test
        @DisplayName("username")
        void username() {
            assertTrue("admin_1".matches(RegexPatterns.USERNAME_PATTERN));
            assertFalse("ab".matches(RegexPatterns.USERNAME_PATTERN));
        }

        @Test
        @DisplayName("password")
        void password() {
            assertTrue("Admin@123".matches(RegexPatterns.PASSWORD_PATTERN));
            assertFalse("Admin123".matches(RegexPatterns.PASSWORD_PATTERN));
        }
    }
}
