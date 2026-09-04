package com.sunrise.dental.repository;

import com.sunrise.dental.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long>, JpaSpecificationExecutor<Patient> {

    Optional<Patient> findByRegNo(String regNo);

    boolean existsByRegNo(String regNo);

    boolean existsByEmail(String email);

    @Query("select p from Patient p where lower(p.firstName) like lower(concat('%', :q, '%')) "
            + "or lower(p.lastName) like lower(concat('%', :q, '%')) "
            + "or p.contactNumber like concat('%', :q, '%') "
            + "or p.regNo like concat('%', upper(:q), '%')")
    Page<Patient> search(@Param("q") String query, Pageable pageable);

    long countByActiveTrue();

    long countByCreatedDateAfter(java.time.LocalDateTime dateTime);
}


