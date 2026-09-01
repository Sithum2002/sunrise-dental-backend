package com.sunrise.dental.specification;

import com.sunrise.dental.entity.Patient;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

/**
 * Dynamic query builder for patients (Specification pattern).
 */
public final class PatientSpecifications {

    private PatientSpecifications() {
    }

    public static Specification<Patient> withFilters(String search, String gender, String bloodGroup) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("firstName")), pattern),
                        cb.like(cb.lower(root.get("lastName")), pattern),
                        cb.like(root.get("regNo"), "%" + search.trim().toUpperCase() + "%"),
                        cb.like(root.get("contactNumber"), "%" + search.trim() + "%")
                ));
            }
            if (gender != null && !gender.isBlank()) {
                predicates.add(cb.equal(root.get("gender"), com.sunrise.dental.enums.Gender.valueOf(gender)));
            }
            if (bloodGroup != null && !bloodGroup.isBlank()) {
                predicates.add(cb.equal(root.get("bloodGroup"), com.sunrise.dental.enums.BloodGroup.valueOf(bloodGroup)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
