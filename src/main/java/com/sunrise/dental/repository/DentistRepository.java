package com.sunrise.dental.repository;

import com.sunrise.dental.entity.Dentist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DentistRepository extends JpaRepository<Dentist, Long>, JpaSpecificationExecutor<Dentist> {

    Optional<Dentist> findByLicenceNo(String licenceNo);

    boolean existsByLicenceNo(String licenceNo);

    boolean existsByEmail(String email);

    List<Dentist> findByStatus(com.sunrise.dental.enums.DentistStatus status);

    long countByStatus(com.sunrise.dental.enums.DentistStatus status);
}
