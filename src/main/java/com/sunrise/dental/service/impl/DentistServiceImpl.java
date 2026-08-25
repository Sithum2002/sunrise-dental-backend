package com.sunrise.dental.service.impl;

import com.sunrise.dental.audit.AuditService;
import com.sunrise.dental.dto.request.DentistRequest;
import com.sunrise.dental.dto.response.DentistResponse;
import com.sunrise.dental.entity.Dentist;
import com.sunrise.dental.enums.DentistStatus;
import com.sunrise.dental.exception.BusinessRuleException;
import com.sunrise.dental.exception.DuplicateResourceException;
import com.sunrise.dental.exception.ResourceNotFoundException;
import com.sunrise.dental.mapper.DentistMapper;
import com.sunrise.dental.repository.DentistRepository;
import com.sunrise.dental.service.DentistService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DentistServiceImpl implements DentistService {

    private final DentistRepository dentistRepository;
    private final DentistMapper dentistMapper;
    private final AuditService auditService;

    @Override
    @Transactional(readOnly = true)
    public List<DentistResponse> getAll(Pageable pageable) {
        return dentistRepository.findAll(pageable).getContent().stream()
                .map(dentistMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DentistResponse getById(Long id) {
        return dentistMapper.toResponse(findDentist(id));
    }

    @Override
    @Transactional
    public DentistResponse create(DentistRequest request) {
        if (dentistRepository.existsByLicenceNo(request.getLicenceNo())) {
            throw new DuplicateResourceException("Licence number already registered: " + request.getLicenceNo());
        }
        if (dentistRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }
        Dentist dentist = dentistMapper.toEntity(request);
        dentistRepository.save(dentist);
        auditService.log("CREATE", "Dentist", dentist.getId(),
                "Added dentist " + dentist.getFirstName() + " " + dentist.getLastName());
        return dentistMapper.toResponse(dentist);
    }

    @Override
    @Transactional
    public DentistResponse update(Long id, DentistRequest request) {
        Dentist dentist = findDentist(id);
        if (!dentist.getEmail().equalsIgnoreCase(request.getEmail())
                && dentistRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }
        dentistMapper.updateEntity(dentist, request);
        dentistRepository.save(dentist);
        auditService.log("UPDATE", "Dentist", id, "Updated dentist record");
        return dentistMapper.toResponse(dentist);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Dentist dentist = findDentist(id);
        dentist.setStatus(DentistStatus.UNAVAILABLE);
        dentistRepository.save(dentist);
        auditService.log("DELETE", "Dentist", id, "Marked dentist unavailable");
    }

    @Override
    @Transactional
    public void updateStatus(Long id, DentistStatus status) {
        Dentist dentist = findDentist(id);
        dentist.setStatus(status);
        dentistRepository.save(dentist);
        auditService.log("UPDATE_STATUS", "Dentist", id, "Dentist status set to " + status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DentistResponse> getAvailable() {
        return dentistRepository.findByStatus(DentistStatus.AVAILABLE).stream()
                .map(dentistMapper::toResponse).toList();
    }

    private Dentist findDentist(Long id) {
        return dentistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dentist not found with id " + id));
    }
}
