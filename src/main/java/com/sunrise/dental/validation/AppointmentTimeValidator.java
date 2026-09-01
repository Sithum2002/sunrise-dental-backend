package com.sunrise.dental.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalTime;

/**
 * Business validation for appointment time slots.
 */
public class AppointmentTimeValidator implements ConstraintValidator<ValidAppointmentTime, LocalTime> {

    public static final LocalTime OPEN = LocalTime.of(8, 0);
    public static final LocalTime LAST_SLOT = LocalTime.of(17, 30);
    public static final LocalTime LUNCH_START = LocalTime.of(12, 30);
    public static final LocalTime LUNCH_END = LocalTime.of(13, 30);

    @Override
    public boolean isValid(LocalTime time, ConstraintValidatorContext context) {
        if (time == null) {
            return true;
        }
        if (time.isBefore(OPEN) || time.isAfter(LAST_SLOT)) {
            return false;
        }
        return time.isBefore(LUNCH_START) || time.isAfter(LUNCH_END);
    }
}
