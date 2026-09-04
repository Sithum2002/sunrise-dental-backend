package com.sunrise.dental.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NumberUtilsTest {

    @Test
    @DisplayName("round rounds half up to 2 decimals")
    void round() {
        assertEquals(7150.0, NumberUtils.round(7150.0));
        assertEquals(10.13, NumberUtils.round(10.126));
        assertEquals(10.12, NumberUtils.round(10.124));
    }

    @Test
    @DisplayName("formatCurrency formats with comma and 2 decimals")
    void formatCurrency() {
        assertEquals("7,150.00", NumberUtils.formatCurrency(7150.0));
        assertEquals("1,234,567.89", NumberUtils.formatCurrency(1234567.891));
    }
}
