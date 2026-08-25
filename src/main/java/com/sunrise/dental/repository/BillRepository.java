package com.sunrise.dental.repository;

import com.sunrise.dental.entity.Bill;
import com.sunrise.dental.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long>, JpaSpecificationExecutor<Bill> {

    Optional<Bill> findByBillNumber(String billNumber);

    boolean existsByBillNumber(String billNumber);

    Optional<Bill> findByAppointmentId(Long appointmentId);

    Page<Bill> findByAppointmentPatientId(Long patientId, Pageable pageable);

    List<Bill> findByBilledAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("select coalesce(sum(b.totalAmount), 0) from Bill b where b.billedAt between :start and :end")
    Double sumTotalBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("select coalesce(sum(b.amountPaid), 0) from Bill b where b.billedAt between :start and :end")
    Double sumPaidBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    long countByPaymentStatus(PaymentStatus status);
}
