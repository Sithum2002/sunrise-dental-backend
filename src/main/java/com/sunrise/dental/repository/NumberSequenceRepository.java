package com.sunrise.dental.repository;

import com.sunrise.dental.entity.NumberSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NumberSequenceRepository extends JpaRepository<NumberSequence, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select n from NumberSequence n where n.sequenceKey = :key")
    Optional<NumberSequence> findByKeyForUpdate(@Param("key") String key);
}
