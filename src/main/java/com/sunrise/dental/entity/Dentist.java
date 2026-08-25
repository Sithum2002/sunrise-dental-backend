package com.sunrise.dental.entity;

import com.sunrise.dental.audit.Auditable;
import com.sunrise.dental.enums.DentistStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Dentist profile registered at the clinic.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "dentists", uniqueConstraints = {
        @UniqueConstraint(name = "uk_dentists_licence", columnNames = "licence_no"),
        @UniqueConstraint(name = "uk_dentists_email", columnNames = "email")
})
public class Dentist extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "licence_no", nullable = false, unique = true, length = 30)
    private String licenceNo;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, length = 120)
    private String specialization;

    @Column(name = "contact_number", nullable = false, length = 20)
    private String contactNumber;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DentistStatus status;

    @Column(name = "years_of_experience")
    private int yearsOfExperience;

    @Column(name = "joining_date")
    private LocalDate joiningDate;

    @Column(length = 500)
    private String biography;
}
