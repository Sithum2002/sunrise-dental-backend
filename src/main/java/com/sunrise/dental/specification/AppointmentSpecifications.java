package com.sunrise.dental.specification;

import com.sunrise.dental.entity.Appointment;
import com.sunrise.dental.enums.AppointmentStatus;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Dynamic query builder for appointments (Specification pattern).
 */
public final class AppointmentSpecifications {

    private AppointmentSpecifications() {
    }

    public static Specification<Appointment> withFilters(LocalDate from, LocalDate to,
                                                         Long dentistId, Long patientId,
                                                         String status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("appointmentDate"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("appointmentDate"), to));
            }
            if (dentistId != null) {
                predicates.add(cb.equal(root.get("dentist").get("id"), dentistId));
            }
            if (patientId != null) {
                predicates.add(cb.equal(root.get("patient").get("id"), patientId));
            }
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), AppointmentStatus.valueOf(status)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
