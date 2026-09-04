package com.sunrise.dental.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AppDateUtilsTest {

    @Test
    @DisplayName("parseDate returns date for valid input")
    void parseDate_valid() {
        assertEquals(LocalDate.of(2026, 1, 15), AppDateUtils.parseDate("2026-01-15"));
    }

    @Test
    @DisplayName("parseDate returns null for blank input")
    void parseDate_blank() {
        assertNull(AppDateUtils.parseDate("  "));
        assertNull(AppDateUtils.parseDate(null));
    }

    @Test
    @DisplayName("parseDate throws for invalid format")
    void parseDate_invalid() {
        assertThrows(IllegalArgumentException.class, () -> AppDateUtils.parseDate("15-01-2026"));
    }

    @Test
    @DisplayName("parseDate trims surrounding whitespace")
    void parseDate_trimmed() {
        assertEquals(LocalDate.of(2026, 1, 15), AppDateUtils.parseDate(" 2026-01-15 "));
    }

    @Test
    @DisplayName("parseDateOrToday returns today when blank")
    void parseDateOrToday_default() {
        assertEquals(LocalDate.now(), AppDateUtils.parseDateOrToday(null));
    }

    @Test
    @DisplayName("parseDateOrToday returns parsed date")
    void parseDateOrToday_parsed() {
        assertEquals(LocalDate.of(2026, 2, 2), AppDateUtils.parseDateOrToday("2026-02-02"));
    }

    @Test
    @DisplayName("startOfDay returns midnight")
    void startOfDay() {
        assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), AppDateUtils.startOfDay(LocalDate.of(2026, 1, 1)));
    }

    @Test
    @DisplayName("endOfDay returns end of day")
    void endOfDay() {
        assertEquals(LocalTime.MAX, AppDateUtils.endOfDay(LocalDate.of(2026, 1, 1)).toLocalTime());
    }

    @Test
    @DisplayName("isWeekend returns true for Saturday and Sunday")
    void isWeekend() {
        assertTrue(AppDateUtils.isWeekend(LocalDate.of(2026, 1, 3)));
        assertTrue(AppDateUtils.isWeekend(LocalDate.of(2026, 1, 4)));
        assertFalse(AppDateUtils.isWeekend(LocalDate.of(2026, 1, 5)));
    }

    @Test
    @DisplayName("formatDate formats and returns empty for null")
    void formatDate() {
        assertEquals("15 Jan 2026", AppDateUtils.formatDate(LocalDate.of(2026, 1, 15)));
        assertEquals("", AppDateUtils.formatDate(null));
    }

    @Test
    @DisplayName("formatTime formats 12 hour")
    void formatTime() {
        assertEquals("09:30 AM", AppDateUtils.formatTime(LocalTime.of(9, 30)));
        assertEquals("", AppDateUtils.formatTime(null));
    }
}
