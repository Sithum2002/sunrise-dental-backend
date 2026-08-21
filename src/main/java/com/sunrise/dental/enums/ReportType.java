package com.sunrise.dental.enums;

// Report types supported by the Jasper Reports engine.
public enum ReportType {
    PATIENT_BILL("Bill / Receipt"),
    APPOINTMENT_LIST("Appointment List"),
    PATIENT_LIST("Patient List"),
    PATIENT_TREATMENT_HISTORY("Patient Treatment History"),
    REVENUE_SUMMARY("Revenue Summary"),
    DENTIST_PERFORMANCE("Dentist Performance"),
    TREATMENT_POPULARITY("Treatment Popularity"),
    MISSED_APPOINTMENTS("Missed / No-show Appointments");

    private final String label;

    ReportType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
