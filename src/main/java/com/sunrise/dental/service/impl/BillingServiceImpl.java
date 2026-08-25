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
import com.sunrise.dental.entity.Payment;
import com.sunrise.dental.enums.AppointmentStatus;
import com.sunrise.dental.enums.PaymentStatus;
import com.sunrise.dental.event.PaymentReceivedEvent;
import com.sunrise.dental.exception.BusinessRuleException;
import com.sunrise.dental.exception.ResourceNotFoundException;
import com.sunrise.dental.mapper.BillMapper;
import com.sunrise.dental.repository.AppointmentRepository;
import com.sunrise.dental.repository.BillRepository;
import com.sunrise.dental.repository.PaymentRepository;
import com.sunrise.dental.service.BillingService;
import com.sunrise.dental.service.NumberSequenceService;
import com.sunrise.dental.specification.BillSpecifications;
import com.sunrise.dental.util.NumberUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Billing service - calculates treatment cost plus consultation fee, applies
 * discount and tax, and tracks payments (partial/full settlement).
 */
@Service
@RequiredArgsConstructor
public class BillingServiceImpl implements BillingService {

    private final BillRepository billRepository;
    private final PaymentRepository paymentRepository;
    private final AppointmentRepository appointmentRepository;
    private final BillMapper billMapper;
    private final NumberSequenceService numberSequenceService;
    private final AuditService auditService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public BillResponse createBill(BillRequest request) {
        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id " + request.getAppointmentId()));

        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new BusinessRuleException("Billing is only allowed for completed appointments.");
        }
        if (billRepository.findByAppointmentId(appointment.getId()).isPresent()) {
            throw new BusinessRuleException("A bill already exists for this appointment.");
        }

        double treatmentCost = appointment.getTreatment().getCost();
        double consultationFee = AppConstants.CONSULTATION_FEE;
        double discount = request.getDiscount() == null ? 0.0 : request.getDiscount();

        if (discount > treatmentCost + consultationFee) {
            throw new BusinessRuleException("Discount cannot exceed the subtotal.");
        }

        double subtotal = treatmentCost + consultationFee - discount;
        double tax = NumberUtils.round(subtotal * AppConstants.TAX_RATE);
        double totalAmount = NumberUtils.round(subtotal + tax);

        Bill bill = Bill.builder()
                .billNumber(numberSequenceService.nextBillNumber())
                .appointment(appointment)
                .treatmentCost(treatmentCost)
                .consultationFee(consultationFee)
                .discount(discount)
                .tax(tax)
                .totalAmount(totalAmount)
                .amountPaid(0.0)
                .dueAmount(totalAmount)
                .paymentStatus(PaymentStatus.UNPAID)
                .billedAt(LocalDateTime.now())
                .build();
        billRepository.save(bill);

        auditService.log("CREATE", "Bill", bill.getId(),
                "Generated bill " + bill.getBillNumber() + " total LKR " + NumberUtils.formatCurrency(totalAmount)
                        + " for appointment " + appointment.getAppointmentNumber());
        return billMapper.toResponse(bill);
    }

    @Override
    @Transactional(readOnly = true)
    public BillResponse getById(Long id) {
        return billMapper.toResponse(findBill(id));
    }

    @Override
    @Transactional(readOnly = true)
    public BillResponse getByBillNumber(String billNumber) {
        Bill bill = billRepository.findByBillNumber(billNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found: " + billNumber));
        return billMapper.toResponse(bill);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BillResponse> getAll(LocalDate from, LocalDate to, Long patientId,
                                             String paymentStatus, Pageable pageable) {
        LocalDateTime fromDateTime = from == null ? null : from.atStartOfDay();
        LocalDateTime toDateTime = to == null ? null : to.atTime(java.time.LocalTime.MAX);
        Page<Bill> page = billRepository.findAll(
                BillSpecifications.withFilters(fromDateTime, toDateTime, patientId, paymentStatus), pageable);
        return new PageResponse<>(
                page.getContent().stream().map(billMapper::toResponse).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    @Override
    @Transactional
    public PaymentResponse recordPayment(PaymentRequest request) {
        Bill bill = findBill(request.getBillId());
        if (bill.getDueAmount() <= 0) {
            throw new BusinessRuleException("This bill has already been fully settled.");
        }
        double amount = NumberUtils.round(request.getAmount());
        if (amount > NumberUtils.round(bill.getDueAmount()) + 0.01) {
            throw new BusinessRuleException("Payment amount exceeds the outstanding balance (LKR "
                    + NumberUtils.formatCurrency(bill.getDueAmount()) + ").");
        }

        Payment payment = Payment.builder()
                .bill(bill)
                .amount(amount)
                .paymentMethod(request.getPaymentMethod())
                .referenceNo(request.getReferenceNo())
                .remarks(request.getRemarks())
                .paymentDate(LocalDateTime.now())
                .build();
        paymentRepository.save(payment);

        double paid = NumberUtils.round(bill.getAmountPaid() + amount);
        double due = NumberUtils.round(bill.getTotalAmount() - paid);
        bill.setAmountPaid(paid);
        bill.setDueAmount(due);
        bill.setPaymentMethod(request.getPaymentMethod());
        if (due <= 0.01) {
            bill.setPaymentStatus(PaymentStatus.PAID);
        } else {
            bill.setPaymentStatus(PaymentStatus.PARTIAL);
        }
        billRepository.save(bill);

        auditService.log("PAYMENT", "Bill", bill.getId(),
                "Received LKR " + NumberUtils.formatCurrency(amount) + " via " + request.getPaymentMethod()
                        + " against bill " + bill.getBillNumber());
        eventPublisher.publishEvent(new PaymentReceivedEvent(bill, amount));
        return toPaymentResponse(payment, bill);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsForBill(Long billId) {
        return paymentRepository.findByBillIdOrderByPaymentDateDesc(billId).stream()
                .map(p -> toPaymentResponse(p, p.getBill())).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BillResponse> getPatientBills(Long patientId, Pageable pageable) {
        Page<Bill> page = billRepository.findByAppointmentPatientId(patientId, pageable);
        return new PageResponse<>(
                page.getContent().stream().map(billMapper::toResponse).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    private Bill findBill(Long id) {
        return billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with id " + id));
    }

    private PaymentResponse toPaymentResponse(Payment payment, Bill bill) {
        return new PaymentResponse(payment.getId(), bill.getId(), bill.getBillNumber(),
                payment.getAmount(), payment.getPaymentMethod(), payment.getReferenceNo(),
                payment.getPaymentDate(), payment.getRemarks());
    }
}
