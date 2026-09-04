package com.sunrise.dental.service.impl;

import com.sunrise.dental.service.SmsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SmsServiceImplTest {

    @Nested
    @DisplayName("When SMS is disabled")
    class Disabled {

        @Test
        @DisplayName("sendSms simulates success")
        void sendSms_disabled() {
            SmsService service = new SmsServiceImpl(false, "Simulator");

            boolean result = service.sendSms("0771234567", "Hello");

            assertTrue(result);
        }

        @Test
        @DisplayName("isConfigured returns false")
        void isConfigured_disabled() {
            SmsService service = new SmsServiceImpl(false, "Simulator");

            assertFalse(service.isConfigured());
        }
    }

    @Nested
    @DisplayName("When SMS is enabled")
    class Enabled {

        @Test
        @DisplayName("sendSms returns true and logs")
        void sendSms_enabled() {
            SmsService service = new SmsServiceImpl(true, "Twilio");

            boolean result = service.sendSms("0771234567", "Hello");

            assertTrue(result);
        }

        @Test
        @DisplayName("isConfigured returns true")
        void isConfigured_enabled() {
            SmsService service = new SmsServiceImpl(true, "Twilio");

            assertTrue(service.isConfigured());
        }

        @Test
        @DisplayName("handles long messages")
        void sendSms_longMessage() {
            SmsService service = new SmsServiceImpl(true, "Twilio");
            String longMessage = "x".repeat(500);

            assertTrue(service.sendSms("0771234567", longMessage));
        }
    }
}
