package com.sunrise.dental.constant;

/**
 * Application-wide constants and clinic profile information.
 */
public final class AppConstants {

    private AppConstants() {
    }

    /** Clinic legal / display name (shown on reports, emails and receipts). */
    public static final String CLINIC_NAME = "Sunrise Dental Clinic";

    /** Clinic street address. */
    public static final String CLINIC_ADDRESS = "No. 210, Galle Road, Colombo 03, Sri Lanka";

    /** Clinic hotline. */
    public static final String CLINIC_PHONE = "+94 11 234 5678";

    /** Clinic email. */
    public static final String CLINIC_EMAIL = "info@sunrisedental.lk";

    /** Clinic registration / TIN used on invoices. */
    public static final String CLINIC_TIN = "TIN-2024-0841";

    /** Currency used across the system. */
    public static final String CURRENCY = "LKR";

    /** Standard consultation fee charged per visit. */
    public static final double CONSULTATION_FEE = 1500.00;

    /** Default tax rate (VAT) applied to bills. */
    public static final double TAX_RATE = 0.10;

    /** Slot length in minutes used to compute appointment end time. */
    public static final int SLOT_MINUTES = 30;

    /** Clinic opening hour (24h). Appointments cannot start before this. */
    public static final int OPENING_HOUR = 8;

    /** Clinic closing hour (24h). Appointments cannot start at/after this. */
    public static final int CLOSING_HOUR = 18;

    /** Default page size for paginated queries. */
    public static final int DEFAULT_PAGE_SIZE = 10;

    /** Prefix used for patient registration numbers. */
    public static final String PATIENT_REG_PREFIX = "SD-P";

    /** Prefix used for appointment numbers. */
    public static final String APPOINTMENT_PREFIX = "AP";

    /** Prefix used for bill numbers. */
    public static final String BILL_PREFIX = "INV";

    /** Prefix used for dentist license numbers. */
    public static final String DENTIST_PREFIX = "DR";

    /** Prefix used for user codes. */
    public static final String USER_PREFIX = "USR";
}
