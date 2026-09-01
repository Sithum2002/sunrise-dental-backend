package com.sunrise.dental.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates that an appointment start time falls within clinic hours
 * (8:00 - 18:00) and does not overlap the lunch break.
 */
@Documented
@Constraint(validatedBy = AppointmentTimeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidAppointmentTime {

    String message() default "Appointment time must be between 08:00 and 17:30 and outside the lunch break (12:30 - 13:30)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
