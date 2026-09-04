package com.sunrise.dental.service.impl;

import com.sunrise.dental.audit.AuditService;
import com.sunrise.dental.constant.AppConstants;
import com.sunrise.dental.dto.request.BillRequest;
import com.sunrise.dental.dto.request.PaymentRequest;
import com.sunrise.dental.dto.response.BillResponse;
import com.sunrise.dental.dto.response.PageResponse;
import com.sunrise.dental.dto.response.PaymentResponse;
import com.sunrise.dental.entity.Appointment;
import com.sunrise.dental.entity.Bill;
import com.sunrise.dental.entity.Dentist;
import com.sunrise.dental.entity.Patient;
import com.sunrise.dental.entity.Payment;
import com.sunrise.dental.entity.Treatment;
import com.sunrise.dental.enums.AppointmentStatus;
import com.sunrise.dental.enums.PaymentMethod;
import com.sunrise.dental.enums.PaymentStatus;
import com.sunrise.dental.event.PaymentReceivedEvent;
import com.sunrise.dental.exception.BusinessRuleException;
import com.sunrise.dental.exception.ResourceNotFoundException;
import com.sunrise.dental.mapper.BillMapper;
import com.sunrise.dental.repository.AppointmentRepository;
import com.sunrise.dental.repository.BillRepository;
import com.sunrise.dental.repository.PaymentRepository;
import com.sunrise.dental.service.NumberSequenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;

@ExtendWith(MockitoExtension.class)
class BillingServiceImplTest {

    @Mock
    private BillRepository billRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private BillMapper billMapper;
    @Mock
    private NumberSequenceService numberSequenceService;
    @Mock
    private AuditService auditService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private BillingServiceImpl billingService;

    private Appointment appointment;
    private Bill bill;
    private Treatment treatment;
    private Patient patient;
    private Dentist dentist;
    private BillResponse billResponse;

    @BeforeEach
    void setUp() {
        patient = Patient.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .contactNumber("0771234567")
                .email("john.doe@example.com")
                .active(true)
                .build();

        dentist = Dentist.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("Smith")
                .build();

        treatment = Treatment.builder()
                .id(3L)
                .name("Dental Cleaning")
                .code("TRT-CLEAN")
                .durationMinutes(30)
                .cost(5000.0)
                .active(true)
                .build();

        appointment = Appointment.builder()
                .id(1L)
                .appointmentNumber("AP-2026-0001")
                .patient(patient)
                .dentist(dentist)
                .treatment(treatment)
                .status(AppointmentStatus.COMPLETED)
                .build();

        bill = Bill.builder()
                .id(1L)
                .billNumber("INV-0001")
                .appointment(appointment)
                .treatmentCost(5000.0)
                .consultationFee(AppConstants.CONSULTATION_FEE)
                .discount(0.0)
                .tax(650.0)
                .totalAmount(7150.0)
                .amountPaid(0.0)
                .dueAmount(7150.0)
                .paymentStatus(PaymentStatus.UNPAID)
                .billedAt(LocalDateTime.now())
                .build();

        billResponse = new BillResponse(
                1L, "INV-0001", 1L, "AP-2026-0001", 1L, "John Doe",
                2L, "Jane Smith", "Dental Cleaning", "TRT-CLEAN",
                null, 5000.0, AppConstants.CONSULTATION_FEE, 0.0, 650.0,
                7150.0, 0.0, 7150.0, PaymentStatus.UNPAID, null, null);
    }

    @Nested
    @DisplayName("createBill()")
    class CreateBill {

        private BillRequest validBillRequest() {
            return BillRequest.builder()
                    .appointmentId(1L)
                    .discount(0.0)
                    .build();
        }

        @Test
        @DisplayName("creates bill for completed appointment")
        void createBill_success() {
            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
            when(billRepository.findByAppointmentId(1L)).thenReturn(Optional.empty());
            when(numberSequenceService.nextBillNumber()).thenReturn("INV-0001");
            when(billRepository.save(any(Bill.class))).thenAnswer(inv -> {
                Bill b = inv.getArgument(0);
                b.setId(1L);
                return b;
            });
            when(billMapper.toResponse(any(Bill.class))).thenReturn(billResponse);

            BillResponse result = billingService.createBill(validBillRequest());

            assertNotNull(result);
            assertEquals("INV-0001", result.billNumber());

            ArgumentCaptor<Bill> billCaptor = ArgumentCaptor.forClass(Bill.class);
            verify(billRepository).save(billCaptor.capture());
            Bill saved = billCaptor.getValue();
            assertEquals(5000.0, saved.getTreatmentCost());
            assertEquals(AppConstants.CONSULTATION_FEE, saved.getConsultationFee());
            assertEquals(650.0, saved.getTax());
            assertEquals(7150.0, saved.getTotalAmount());
            assertEquals(PaymentStatus.UNPAID, saved.getPaymentStatus());

            verify(auditService).log(eq("CREATE"), eq("Bill"), eq(1L), anyString());
        }

        @Test
        @DisplayName("applies discount correctly")
        void createBill_withDiscount() {
            BillRequest request = BillRequest.builder()
                    .appointmentId(1L)
                    .discount(500.0)
                    .build();
            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
            when(billRepository.findByAppointmentId(1L)).thenReturn(Optional.empty());
            when(numberSequenceService.nextBillNumber()).thenReturn("INV-0001");
            when(billRepository.save(any(Bill.class))).thenReturn(bill);
            when(billMapper.toResponse(any(Bill.class))).thenReturn(billResponse);

            billingService.createBill(request);

            ArgumentCaptor<Bill> captor = ArgumentCaptor.forClass(Bill.class);
            verify(billRepository).save(captor.capture());
            Bill saved = captor.getValue();
            assertEquals(500.0, saved.getDiscount());
            double subtotal = 5000.0 + AppConstants.CONSULTATION_FEE - 500.0;
            double expectedTax = Math.round(subtotal * AppConstants.TAX_RATE * 100.0) / 100.0;
            assertEquals(expectedTax, saved.getTax());
            assertEquals(subtotal + expectedTax, saved.getTotalAmount());
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when appointment not found")
        void createBill_appointmentNotFound() {
            when(appointmentRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> billingService.createBill(BillRequest.builder().appointmentId(99L).build()));
        }

        @Test
        @DisplayName("throws BusinessRuleException when appointment is not completed")
        void createBill_notCompleted() {
            appointment.setStatus(AppointmentStatus.SCHEDULED);
            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

            BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                    () -> billingService.createBill(validBillRequest()));
            assertTrue(ex.getMessage().contains("only allowed for completed"));
        }

        @Test
        @DisplayName("throws BusinessRuleException when bill already exists")
        void createBill_duplicateBill() {
            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
            when(billRepository.findByAppointmentId(1L)).thenReturn(Optional.of(bill));

            BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                    () -> billingService.createBill(validBillRequest()));
            assertTrue(ex.getMessage().contains("already exists"));
        }

        @Test
        @DisplayName("throws BusinessRuleException when discount exceeds subtotal")
        void createBill_discountTooLarge() {
            BillRequest request = BillRequest.builder()
                    .appointmentId(1L)
                    .discount(100000.0)
                    .build();
            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
            when(billRepository.findByAppointmentId(1L)).thenReturn(Optional.empty());

            BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                    () -> billingService.createBill(request));
            assertTrue(ex.getMessage().contains("Discount cannot exceed"));
        }

        @Test
        @DisplayName("handles null discount as zero")
        void createBill_nullDiscount() {
            BillRequest request = BillRequest.builder()
                    .appointmentId(1L)
                    .build();
            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
            when(billRepository.findByAppointmentId(1L)).thenReturn(Optional.empty());
            when(numberSequenceService.nextBillNumber()).thenReturn("INV-0001");
            when(billRepository.save(any(Bill.class))).thenReturn(bill);
            when(billMapper.toResponse(any(Bill.class))).thenReturn(billResponse);

            billingService.createBill(request);

            ArgumentCaptor<Bill> captor = ArgumentCaptor.forClass(Bill.class);
            verify(billRepository).save(captor.capture());
            assertEquals(0.0, captor.getValue().getDiscount());
        }
    }

    @Nested
    @DisplayName("getById() / getByBillNumber()")
    class GetBill {

        @Test
        @DisplayName("returns bill by id")
        void getById_success() {
            when(billRepository.findById(1L)).thenReturn(Optional.of(bill));
            when(billMapper.toResponse(bill)).thenReturn(billResponse);

            BillResponse result = billingService.getById(1L);

            assertNotNull(result);
            assertEquals("INV-0001", result.billNumber());
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when bill not found by id")
        void getById_notFound() {
            when(billRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> billingService.getById(99L));
        }

        @Test
        @DisplayName("returns bill by number")
        void getByBillNumber_success() {
            when(billRepository.findByBillNumber("INV-0001")).thenReturn(Optional.of(bill));
            when(billMapper.toResponse(bill)).thenReturn(billResponse);

            BillResponse result = billingService.getByBillNumber("INV-0001");

            assertEquals("INV-0001", result.billNumber());
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when bill number not found")
        void getByBillNumber_notFound() {
            when(billRepository.findByBillNumber("NOPE")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> billingService.getByBillNumber("NOPE"));
        }
    }

    @Nested
    @DisplayName("getAll()")
    class GetAll {

        @Test
        @DisplayName("returns paginated bills")
        void getAll_success() {
            Page<Bill> page = new PageImpl<>(List.of(bill));
            Pageable pageable = PageRequest.of(0, 10);
            when(billRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
            when(billMapper.toResponse(bill)).thenReturn(billResponse);

            PageResponse<BillResponse> result = billingService.getAll(null, null, null, null, pageable);

            assertNotNull(result);
            assertEquals(1, result.content().size());
            assertEquals(1, result.totalElements());
        }

        @Test
        @DisplayName("returns empty page when no bills")
        void getAll_empty() {
            Page<Bill> empty = new PageImpl<>(List.of());
            Pageable pageable = PageRequest.of(0, 10);
            when(billRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(empty);

            PageResponse<BillResponse> result = billingService.getAll(null, null, null, null, pageable);

            assertTrue(result.content().isEmpty());
        }
    }

    @Nested
    @DisplayName("recordPayment()")
    class RecordPayment {

        private PaymentRequest validPaymentRequest() {
            return PaymentRequest.builder()
                    .billId(1L)
                    .amount(5000.0)
                    .paymentMethod(PaymentMethod.CASH)
                    .build();
        }

        @Test
        @DisplayName("records a partial payment")
        void recordPayment_partial() {
            when(billRepository.findById(1L)).thenReturn(Optional.of(bill));
            when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
            when(billRepository.save(any(Bill.class))).thenReturn(bill);

            PaymentResponse result = billingService.recordPayment(validPaymentRequest());

            assertNotNull(result);
            assertEquals(5000.0, result.amount());
            assertEquals(PaymentMethod.CASH, result.paymentMethod());
            assertEquals(5000.0, bill.getAmountPaid());
            assertEquals(2150.0, bill.getDueAmount());
            assertEquals(PaymentStatus.PARTIAL, bill.getPaymentStatus());
            verify(auditService).log(eq("PAYMENT"), eq("Bill"), eq(1L), anyString());
            verify(eventPublisher).publishEvent(any(PaymentReceivedEvent.class));
        }

        @Test
        @DisplayName("marks bill as PAID when fully settled")
        void recordPayment_fullSettlement() {
            PaymentRequest request = PaymentRequest.builder()
                    .billId(1L)
                    .amount(7150.0)
                    .paymentMethod(PaymentMethod.CARD)
                    .build();
            when(billRepository.findById(1L)).thenReturn(Optional.of(bill));
            when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
            when(billRepository.save(any(Bill.class))).thenReturn(bill);

            billingService.recordPayment(request);

            assertEquals(PaymentStatus.PAID, bill.getPaymentStatus());
            assertEquals(0.0, bill.getDueAmount());
        }

        @Test
        @DisplayName("throws BusinessRuleException when bill fully settled")
        void recordPayment_billSettled() {
            bill.setDueAmount(0.0);
            when(billRepository.findById(1L)).thenReturn(Optional.of(bill));

            BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                    () -> billingService.recordPayment(validPaymentRequest()));
            assertTrue(ex.getMessage().contains("already been fully settled"));
        }

        @Test
        @DisplayName("throws BusinessRuleException when amount exceeds due")
        void recordPayment_exceedsDue() {
            PaymentRequest request = PaymentRequest.builder()
                    .billId(1L)
                    .amount(100000.0)
                    .paymentMethod(PaymentMethod.CASH)
                    .build();
            when(billRepository.findById(1L)).thenReturn(Optional.of(bill));

            BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                    () -> billingService.recordPayment(request));
            assertTrue(ex.getMessage().contains("exceeds the outstanding balance"));
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when bill not found")
        void recordPayment_billNotFound() {
            when(billRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> billingService.recordPayment(PaymentRequest.builder().billId(99L)
                            .amount(10.0).paymentMethod(PaymentMethod.CASH).build()));
        }

        @Test
        @DisplayName("sets bill payment method and reference")
        void recordPayment_setsMethod() {
            PaymentRequest request = PaymentRequest.builder()
                    .billId(1L)
                    .amount(100.0)
                    .paymentMethod(PaymentMethod.BANK_TRANSFER)
                    .referenceNo("TXN-123")
                    .remarks("Online")
                    .build();
            when(billRepository.findById(1L)).thenReturn(Optional.of(bill));
            when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
            when(billRepository.save(any(Bill.class))).thenReturn(bill);

            billingService.recordPayment(request);

            assertEquals(PaymentMethod.BANK_TRANSFER, bill.getPaymentMethod());
        }
    }

    @Nested
    @DisplayName("getPaymentsForBill() / getPatientBills()")
    class PaymentQueries {

        @Test
        @DisplayName("returns payments for bill")
        void getPaymentsForBill() {
            Payment payment = Payment.builder()
                    .id(1L)
                    .bill(bill)
                    .amount(5000.0)
                    .paymentMethod(PaymentMethod.CASH)
                    .build();
            when(paymentRepository.findByBillIdOrderByPaymentDateDesc(1L)).thenReturn(List.of(payment));

            List<PaymentResponse> result = billingService.getPaymentsForBill(1L);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("INV-0001", result.get(0).billNumber());
        }

        @Test
        @DisplayName("returns empty list if no payments")
        void getPaymentsForBill_empty() {
            when(paymentRepository.findByBillIdOrderByPaymentDateDesc(1L)).thenReturn(List.of());

            List<PaymentResponse> result = billingService.getPaymentsForBill(1L);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("returns patient bills")
        void getPatientBills() {
            Page<Bill> page = new PageImpl<>(List.of(bill));
            Pageable pageable = PageRequest.of(0, 10);
            when(billRepository.findByAppointmentPatientId(1L, pageable)).thenReturn(page);
            when(billMapper.toResponse(bill)).thenReturn(billResponse);

            PageResponse<BillResponse> result = billingService.getPatientBills(1L, pageable);

            assertNotNull(result);
            assertEquals(1, result.totalElements());
        }

        @Test
        @DisplayName("returns empty patient bills when none exist")
        void getPatientBills_empty() {
            Page<Bill> empty = new PageImpl<>(List.of());
            Pageable pageable = PageRequest.of(0, 10);
            when(billRepository.findByAppointmentPatientId(99L, pageable)).thenReturn(empty);

            PageResponse<BillResponse> result = billingService.getPatientBills(99L, pageable);

            assertTrue(result.content().isEmpty());
        }
    }
}
