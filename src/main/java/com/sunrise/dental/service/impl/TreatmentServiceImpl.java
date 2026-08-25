package com.sunrise.dental.service.impl;

import com.sunrise.dental.audit.AuditService;
import com.sunrise.dental.dto.request.TreatmentRequest;
import com.sunrise.dental.dto.response.TreatmentResponse;
import com.sunrise.dental.entity.Treatment;
import com.sunrise.dental.exception.DuplicateResourceException;
import com.sunrise.dental.exception.ResourceNotFoundException;
import com.sunrise.dental.mapper.TreatmentMapper;
import com.sunrise.dental.repository.TreatmentRepository;
import com.sunrise.dental.service.TreatmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TreatmentServiceImpl implements TreatmentService {

    private final TreatmentRepository treatmentRepository;
    private final TreatmentMapper treatmentMapper;
    private final AuditService auditService;

    @Override
    @Transactional(readOnly = true)
    public List<TreatmentResponse> getAll(boolean onlyActive) {
        if (onlyActive) {
            return treatmentRepository.findByActiveTrueOrderByNameAsc().stream()
                    .map(treatmentMapper::toResponse).toList();
        }
        return treatmentRepository.findAll().stream()
                .sorted(java.util.Comparator.comparing(Treatment::getName))
                .map(treatmentMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TreatmentResponse getById(Long id) {
        return treatmentMapper.toResponse(findTreatment(id));
    }

    @Override
    @Transactional
    public TreatmentResponse create(TreatmentRequest request) {
        if (treatmentRepository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException("Treatment code already exists: " + request.getCode());
        }
        Treatment treatment = treatmentMapper.toEntity(request);
        treatmentRepository.save(treatment);
        auditService.log("CREATE", "Treatment", treatment.getId(), "Added treatment " + treatment.getName());
        return treatmentMapper.toResponse(treatment);
    }

    @Override
    @Transactional
    public TreatmentResponse update(Long id, TreatmentRequest request) {
        Treatment treatment = findTreatment(id);
        treatmentMapper.updateEntity(treatment, request);
        treatmentRepository.save(treatment);
        auditService.log("UPDATE", "Treatment", id, "Updated treatment " + treatment.getName());
        return treatmentMapper.toResponse(treatment);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Treatment treatment = findTreatment(id);
        treatment.setActive(false);
        treatmentRepository.save(treatment);
        auditService.log("DELETE", "Treatment", id, "Deactivated treatment " + treatment.getName());
    }

    private Treatment findTreatment(Long id) {
        return treatmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Treatment not found with id " + id));
    }
}
