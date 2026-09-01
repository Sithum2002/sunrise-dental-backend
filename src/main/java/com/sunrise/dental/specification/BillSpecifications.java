package com.sunrise.dental.specification;

import com.sunrise.dental.entity.Bill;
import com.sunrise.dental.enums.PaymentStatus;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Dynamic query builder for bills (Specification pattern).
 */
public final class BillSpecifications {

    private BillSpecifications() {
    }

    public static Specification<Bill> withFilters(LocalDateTime from, LocalDateTime to,
                                                  Long patientId, String paymentStatus) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("billedAt"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("billedAt"), to));
            }
            if (patientId != null) {
                predicates.add(cb.equal(root.get("appointment").get("patient").get("id"), patientId));
            }
            if (paymentStatus != null && !paymentStatus.isBlank()) {
                predicates.add(cb.equal(root.get("paymentStatus"), PaymentStatus.valueOf(paymentStatus)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
