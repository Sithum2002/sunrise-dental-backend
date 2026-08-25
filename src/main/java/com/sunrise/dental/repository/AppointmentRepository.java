package com.sunrise.dental.repository;

import com.sunrise.dental.entity.Appointment;
import com.sunrise.dental.enums.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long>,
        JpaSpecificationExecutor<Appointment> {

    Optional<Appointment> findByAppointmentNumber(String appointmentNumber);

    boolean existsByAppointmentNumber(String appointmentNumber);

    @Query("select count(a) > 0 from Appointment a where a.dentist.id = :dentistId "
            + "and a.appointmentDate = :date and a.startTime = :startTime and a.status <> 'CANCELLED'")
    boolean existsOverlappingSlot(@Param("dentistId") Long dentistId,
                                  @Param("date") LocalDate date,
                                  @Param("startTime") java.time.LocalTime startTime);

    @Query("select count(a) > 0 from Appointment a where a.dentist.id = :dentistId "
            + "and a.appointmentDate = :date and a.startTime < :endTime and a.endTime > :startTime "
            + "and a.id <> :excludeId and a.status <> 'CANCELLED'")
    boolean existsOverlappingRange(@Param("dentistId") Long dentistId,
                                   @Param("date") LocalDate date,
                                   @Param("startTime") java.time.LocalTime startTime,
                                   @Param("endTime") java.time.LocalTime endTime,
                                   @Param("excludeId") Long excludeId);

    List<Appointment> findByAppointmentDate(LocalDate date);

    List<Appointment> findByAppointmentDateBetweenOrderByStartTimeAsc(LocalDate start, LocalDate end);

    List<Appointment> findByStatusAndAppointmentDate(AppointmentStatus status, LocalDate date);

    Page<Appointment> findByPatientIdOrderByAppointmentDateDesc(Long patientId, Pageable pageable);

    long countByAppointmentDate(LocalDate date);

    long countByAppointmentDateAndStatus(LocalDate date, AppointmentStatus status);

    long countByStatus(AppointmentStatus status);

    @Query("select count(a) from Appointment a where a.createdDate >= :since")
    long countCreatedSince(@Param("since") LocalDateTime since);

    @Query("select a from Appointment a where a.status in ('SCHEDULED','CONFIRMED') "
            + "and a.appointmentDate = :date and a.startTime >= :time")
    List<Appointment> findUpcomingToday(@Param("date") LocalDate date, @Param("time") java.time.LocalTime time);
}
