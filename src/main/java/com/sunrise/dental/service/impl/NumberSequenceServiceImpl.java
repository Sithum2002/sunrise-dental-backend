package com.sunrise.dental.service.impl;

import com.sunrise.dental.constant.AppConstants;
import com.sunrise.dental.entity.NumberSequence;
import com.sunrise.dental.repository.NumberSequenceRepository;
import com.sunrise.dental.service.NumberSequenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Year;

/**
 * Allocates sequential numbers using a pessimistic-locked counter row
 * (concurrency-safe; prevents duplicate appointment/bill/reg numbers).
 */
@Service
@RequiredArgsConstructor
public class NumberSequenceServiceImpl implements NumberSequenceService {

    private final NumberSequenceRepository numberSequenceRepository;

    @Override
    @Transactional
    public String nextPatientRegNo() {
        long value = nextValue("PATIENT", AppConstants.PATIENT_REG_PREFIX);
        return AppConstants.PATIENT_REG_PREFIX + value;
    }

    @Override
    @Transactional
    public String nextAppointmentNumber() {
        long value = nextValue("APPOINTMENT", AppConstants.APPOINTMENT_PREFIX);
        return AppConstants.APPOINTMENT_PREFIX + "-" + Year.now() + "-" + value;
    }

    @Override
    @Transactional
    public String nextBillNumber() {
        long value = nextValue("BILL", AppConstants.BILL_PREFIX);
        return AppConstants.BILL_PREFIX + "-" + value;
    }

    private long nextValue(String key, String prefix) {
        NumberSequence sequence = numberSequenceRepository.findByKeyForUpdate(key)
                .orElseGet(() -> numberSequenceRepository.save(
                        NumberSequence.builder().sequenceKey(key).currentValue(0).prefix(prefix).build()));
        sequence.setCurrentValue(sequence.getCurrentValue() + 1);
        numberSequenceRepository.save(sequence);
        return sequence.getCurrentValue();
    }
}
