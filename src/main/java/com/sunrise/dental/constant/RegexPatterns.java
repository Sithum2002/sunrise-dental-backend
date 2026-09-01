package com.sunrise.dental.constant;

/**
 * Centralised regular expressions used by the validation layer.
 */
public final class RegexPatterns {

    private RegexPatterns() {
    }

    /** Sri Lankan mobile / landline (with optional country code +94 or 0). */
    public static final String PHONE_PATTERN = "^(\\+94|0)?7[0-9]{8}$";

    /** General Sri Lankan phone allowing mobile and landline formats. */
    public static final String PHONE_GENERAL_PATTERN = "^(\\+94|0)[0-9]{9}$";

    /** Appointment number format: AP-YYYY-####. */
    public static final String APPOINTMENT_NUMBER_PATTERN = "^AP-[0-9]{4}-[0-9]{1,6}$";

    /** Patient registration number format: SD-P####. */
    public static final String PATIENT_REG_NUMBER_PATTERN = "^SD-P[0-9]{1,6}$";

    /** Bill number format: INV-####. */
    public static final String BILL_NUMBER_PATTERN = "^INV-[0-9]{1,6}$";

    /** Username: letters, digits, underscore, hyphen, 3-30 chars. */
    public static final String USERNAME_PATTERN = "^[a-zA-Z0-9_.-]{3,30}$";

    /** Strong password: 8-64 chars with at least one letter, one digit and one special char. */
    public static final String PASSWORD_PATTERN = "^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,64}$";

    /** Licence number for dentists: DR-####. */
    public static final String LICENCE_PATTERN = "^DR-[0-9]{1,6}$";
}
