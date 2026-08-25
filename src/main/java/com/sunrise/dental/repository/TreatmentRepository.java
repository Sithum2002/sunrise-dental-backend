package com.sunrise.dental.repository;

import com.sunrise.dental.entity.Treatment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TreatmentRepository extends JpaRepository<Treatment, Long> {

    Optional<Treatment> findByCode(String code);

    boolean existsByCode(String code);

    List<Treatment> findByActiveTrueOrderByNameAsc();
}
