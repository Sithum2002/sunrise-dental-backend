package com.sunrise.dental.service.impl;

import com.sunrise.dental.audit.AuditService;
import com.sunrise.dental.dto.request.PatientRequest;
import com.sunrise.dental.dto.response.AppointmentResponse;
import com.sunrise.dental.dto.response.PageResponse;
import com.sunrise.dental.dto.response.PatientResponse;
import com.sunrise.dental.entity.Appointment;
import com.sunrise.dental.entity.Patient;
import com.sunrise.dental.exception.DuplicateResourceException;
import com.sunrise.dental.exception.ResourceNotFoundException;
import com.sunrise.dental.mapper.AppointmentMapper;
import com.sunrise.dental.mapper.PatientMapper;
import com.sunrise.dental.repository.AppointmentRepository;
import com.sunrise.dental.repository.PatientRepository;
import com.sunrise.dental.service.NumberSequenceService;
import com.sunrise.dental.service.PatientService;
import com.sunrise.dental.specification.PatientSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final PatientMapper patientMapper;
    private final AppointmentMapper appointmentMapper;
    private final NumberSequenceService numberSequenceService;
    private final AuditService auditService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PatientResponse> getAll(Pageable pageable, String search, String gender, String bloodGroup) {
        com.sunrise.dental.enums.Gender parsedGender = safeEnum(gender, com.sunrise.dental.enums.Gender.class);
        com.sunrise.dental.enums.BloodGroup parsedBlood = safeEnum(bloodGroup, com.sunrise.dental.enums.BloodGroup.class);
        Page<Patient> page = patientRepository.findAll(
                PatientSpecifications.withFilters(search,
                        parsedGender == null ? null : parsedGender.name(),
                        parsedBlood == null ? null : parsedBlood.name()),
                pageable);
        return new PageResponse<>(
                page.getContent().stream().map(patientMapper::toResponse).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    @Override
    @Transactional(readOnly = true)
    public PatientResponse getById(Long id) {
        return patientMapper.toResponse(findPatient(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PatientResponse getByRegNo(String regNo) {
        Patient patient = patientRepository.findByRegNo(regNo)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with reg no " + regNo));
        return patientMapper.toResponse(patient);
    }

    @Override
    @Transactional
    public PatientResponse create(PatientRequest request) {
        if (patientRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("A patient is already registered with email " + request.getEmail());
        }
        if (request.getDateOfBirth().isAfter(LocalDate.now().minusYears(18))) {
            // minors are allowed but flagged - clinic requires guardian consent
            auditService.log("CREATE", "Patient", null,
                    "Minor patient registered (age " + age(request.getDateOfBirth()) + ")");
        }
        Patient patient = patientMapper.toEntity(request);
        patient.setRegNo(numberSequenceService.nextPatientRegNo());
        patient.setActive(true);
        patientRepository.save(patient);
        auditService.log("CREATE", "Patient", patient.getId(),
                "Registered patient " + patient.getFullName() + " (" + patient.getRegNo() + ")");
        return patientMapper.toResponse(patient);
    }

    @Override
    @Transactional
    public PatientResponse update(Long id, PatientRequest request) {
        Patient patient = findPatient(id);
        if (!patient.getEmail().equalsIgnoreCase(request.getEmail())
                && patientRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("A patient is already registered with email " + request.getEmail());
        }
        patientMapper.updateEntity(patient, request);
        patientRepository.save(patient);
        auditService.log("UPDATE", "Patient", id, "Updated patient record " + patient.getFullName());
        return patientMapper.toResponse(patient);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Patient patient = findPatient(id);
        patient.setActive(false);
        patientRepository.save(patient);
        auditService.log("DELETE", "Patient", id, "Deactivated patient " + patient.getFullName());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getTreatmentHistory(Long patientId) {
        List<Appointment> appointments =
                appointmentRepository.findByPatientIdOrderByAppointmentDateDesc(patientId, Pageable.unpaged()).getContent();
        return appointments.stream().map(appointmentMapper::toResponse).toList();
    }

    private Patient findPatient(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id " + id));
    }

    private int age(LocalDate dateOfBirth) {
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    private <E extends Enum<E>> E safeEnum(String value, Class<E> clazz) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Enum.valueOf(clazz, value);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid value '" + value + "' for " + clazz.getSimpleName());
        }
    }
}
