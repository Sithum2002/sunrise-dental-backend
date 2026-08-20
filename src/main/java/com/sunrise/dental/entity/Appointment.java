package com.sunrise.dental.entity;

import com.sunrise.dental.audit.Auditable;
import com.sunrise.dental.enums.AppointmentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * A scheduled patient visit. Appointment numbers are unique and generated
 * by the system (AP-yyyy-####).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "appointments",
        indexes = {
                @Index(name = "idx_appointment_number", columnList = "appointment_number", unique = true),
                @Index(name = "idx_appointment_date", columnList = "appointment_date"),
                @Index(name = "idx_appointment_status", columnList = "status")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_appointment_slot",
                        columnNames = {"dentist_id", "appointment_date", "start_time"})
        })
public class Appointment extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "appointment_number", nullable = false, length = 30)
    private String appointmentNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dentist_id", nullable = false)
    private Dentist dentist;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "treatment_id", nullable = false)
    private Treatment treatment;

    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AppointmentStatus status;

    @Column(length = 1000)
    private String notes;

    @Column(name = "completed_notes", length = 1000)
    private String completedNotes;
}
