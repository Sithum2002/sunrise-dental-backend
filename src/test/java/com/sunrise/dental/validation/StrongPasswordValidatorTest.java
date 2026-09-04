package com.sunrise.dental.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StrongPasswordValidatorTest {

    private StrongPasswordValidator validator;

    @BeforeEach
    void setUp() {
        validator = new StrongPasswordValidator();
    }

    @Nested
    @DisplayName("isValid")
    class IsValid {

        @Test
        @DisplayName("passes null")
        void nullIsValid() {
            assertTrue(validator.isValid(null, null));
        }

        @Test
        @DisplayName("accepts a strong password")
        void strong() {
            assertTrue(validator.isValid("Admin@123", null));
        }

        @Test
        @DisplayName("rejects password without digit")
        void noDigit() {
            assertFalse(validator.isValid("Admin@pass", null));
        }

        @Test
        @DisplayName("rejects password without letter")
        void noLetter() {
            assertFalse(validator.isValid("12345@678", null));
        }

        @Test
        @DisplayName("rejects password without special char")
        void noSpecial() {
            assertFalse(validator.isValid("Admin1234", null));
        }

        @Test
        @DisplayName("rejects short password")
        void shortPassword() {
            assertFalse(validator.isValid("A1@", null));
        }

        @Test
        @DisplayName("rejects long password over 64 chars")
        void longPassword() {
            assertFalse(validator.isValid("A1@" + "a".repeat(65), null));
        }

        @Test
        @DisplayName("accepts lowercase numeric special")
        void lowercase() {
            assertTrue(validator.isValid("user@1234", null));
        }
    }
}
