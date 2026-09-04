package com.sunrise.dental.service.impl;

import com.sunrise.dental.entity.NumberSequence;
import com.sunrise.dental.repository.NumberSequenceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Year;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NumberSequenceServiceImplTest {

    @Mock
    private NumberSequenceRepository numberSequenceRepository;

    @InjectMocks
    private NumberSequenceServiceImpl numberSequenceService;

    @Nested
    @DisplayName("nextPatientRegNo()")
    class NextPatientRegNo {

        @Test
        @DisplayName("generates first patient reg number")
        void nextPatientRegNo_first() {
            NumberSequence sequence = NumberSequence.builder()
                    .sequenceKey("PATIENT").prefix("SD-P").currentValue(0).build();
            when(numberSequenceRepository.findByKeyForUpdate("PATIENT")).thenReturn(Optional.of(sequence));
            when(numberSequenceRepository.save(any(NumberSequence.class))).thenAnswer(inv -> inv.getArgument(0));

            String result = numberSequenceService.nextPatientRegNo();

            assertEquals("SD-P1", result);
            assertEquals(1, sequence.getCurrentValue());
        }

        @Test
        @DisplayName("generates sequential patient reg numbers")
        void nextPatientRegNo_sequential() {
            NumberSequence sequence = NumberSequence.builder()
                    .sequenceKey("PATIENT").prefix("SD-P").currentValue(10).build();
            when(numberSequenceRepository.findByKeyForUpdate("PATIENT")).thenReturn(Optional.of(sequence));
            when(numberSequenceRepository.save(any(NumberSequence.class))).thenAnswer(inv -> inv.getArgument(0));

            String first = numberSequenceService.nextPatientRegNo();
            String second = numberSequenceService.nextPatientRegNo();

            assertEquals("SD-P11", first);
            assertEquals("SD-P12", second);
        }

        @Test
        @DisplayName("creates new sequence row when missing")
        void nextPatientRegNo_createsNew() {
            when(numberSequenceRepository.findByKeyForUpdate("PATIENT")).thenReturn(Optional.empty());
            when(numberSequenceRepository.save(any(NumberSequence.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            String result = numberSequenceService.nextPatientRegNo();

            assertEquals("SD-P1", result);
            verify(numberSequenceRepository, times(2)).save(argThat(s ->
                    "PATIENT".equals(s.getSequenceKey()) && "SD-P".equals(s.getPrefix())));
        }
    }

    @Nested
    @DisplayName("nextAppointmentNumber()")
    class NextAppointmentNumber {

        @Test
        @DisplayName("generates appointment number with current year")
        void nextAppointmentNumber() {
            NumberSequence sequence = NumberSequence.builder()
                    .sequenceKey("APPOINTMENT").prefix("AP").currentValue(42).build();
            when(numberSequenceRepository.findByKeyForUpdate("APPOINTMENT")).thenReturn(Optional.of(sequence));
            when(numberSequenceRepository.save(any(NumberSequence.class))).thenAnswer(inv -> inv.getArgument(0));

            String result = numberSequenceService.nextAppointmentNumber();

            assertEquals("AP-" + Year.now() + "-43", result);
            assertEquals(43, sequence.getCurrentValue());
        }

        @Test
        @DisplayName("increments appointment counter each call")
        void nextAppointmentNumber_sequential() {
            NumberSequence sequence = NumberSequence.builder()
                    .sequenceKey("APPOINTMENT").prefix("AP").currentValue(0).build();
            when(numberSequenceRepository.findByKeyForUpdate("APPOINTMENT")).thenReturn(Optional.of(sequence));
            when(numberSequenceRepository.save(any(NumberSequence.class))).thenAnswer(inv -> inv.getArgument(0));

            String first = numberSequenceService.nextAppointmentNumber();
            String second = numberSequenceService.nextAppointmentNumber();

            assertEquals("AP-" + Year.now() + "-1", first);
            assertEquals("AP-" + Year.now() + "-2", second);
        }
    }

    @Nested
    @DisplayName("nextBillNumber()")
    class NextBillNumber {

        @Test
        @DisplayName("generates bill number")
        void nextBillNumber() {
            NumberSequence sequence = NumberSequence.builder()
                    .sequenceKey("BILL").prefix("INV").currentValue(99).build();
            when(numberSequenceRepository.findByKeyForUpdate("BILL")).thenReturn(Optional.of(sequence));
            when(numberSequenceRepository.save(any(NumberSequence.class))).thenAnswer(inv -> inv.getArgument(0));

            String result = numberSequenceService.nextBillNumber();

            assertEquals("INV-100", result);
        }

        @Test
        @DisplayName("increments bill counter")
        void nextBillNumber_increments() {
            NumberSequence sequence = NumberSequence.builder()
                    .sequenceKey("BILL").prefix("INV").currentValue(0).build();
            when(numberSequenceRepository.findByKeyForUpdate("BILL")).thenReturn(Optional.of(sequence));
            when(numberSequenceRepository.save(any(NumberSequence.class))).thenAnswer(inv -> inv.getArgument(0));

            numberSequenceService.nextBillNumber();
            numberSequenceService.nextBillNumber();

            assertEquals(2, sequence.getCurrentValue());
        }
    }
}
