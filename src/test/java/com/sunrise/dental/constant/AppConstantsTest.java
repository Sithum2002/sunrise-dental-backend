package com.sunrise.dental.constant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AppConstantsTest {

    @Test
    @DisplayName("clinic profile constants")
    void clinicProfile() {
        assertEquals("Sunrise Dental Clinic", AppConstants.CLINIC_NAME);
        assertEquals("No. 210, Galle Road, Colombo 03, Sri Lanka", AppConstants.CLINIC_ADDRESS);
        assertEquals("+94 11 234 5678", AppConstants.CLINIC_PHONE);
        assertEquals("info@sunrisedental.lk", AppConstants.CLINIC_EMAIL);
        assertEquals("TIN-2024-0841", AppConstants.CLINIC_TIN);
        assertEquals("LKR", AppConstants.CURRENCY);
    }

    @Test
    @DisplayName("billing constants")
    void billing() {
        assertEquals(1500.00, AppConstants.CONSULTATION_FEE);
        assertEquals(0.10, AppConstants.TAX_RATE);
    }

    @Test
    @DisplayName("clinic hours and slot constants")
    void hours() {
        assertEquals(30, AppConstants.SLOT_MINUTES);
        assertEquals(8, AppConstants.OPENING_HOUR);
        assertEquals(18, AppConstants.CLOSING_HOUR);
        assertEquals(10, AppConstants.DEFAULT_PAGE_SIZE);
    }

    @Test
    @DisplayName("number prefixes")
    void prefixes() {
        assertEquals("SD-P", AppConstants.PATIENT_REG_PREFIX);
        assertEquals("AP", AppConstants.APPOINTMENT_PREFIX);
        assertEquals("INV", AppConstants.BILL_PREFIX);
        assertEquals("DR", AppConstants.DENTIST_PREFIX);
        assertEquals("USR", AppConstants.USER_PREFIX);
    }
}
