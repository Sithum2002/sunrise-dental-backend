package com.sunrise.dental.service;

import com.sunrise.dental.dto.request.BillRequest;
import com.sunrise.dental.dto.request.PaymentRequest;
import com.sunrise.dental.dto.response.BillResponse;
import com.sunrise.dental.dto.response.PageResponse;
import com.sunrise.dental.dto.response.PaymentResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface BillingService {

    BillResponse createBill(BillRequest request);

    BillResponse getById(Long id);

    BillResponse getByBillNumber(String billNumber);

    PageResponse<BillResponse> getAll(LocalDate from, LocalDate to, Long patientId,
                                      String paymentStatus, Pageable pageable);

    PaymentResponse recordPayment(PaymentRequest request);

    java.util.List<PaymentResponse> getPaymentsForBill(Long billId);

    PageResponse<BillResponse> getPatientBills(Long patientId, Pageable pageable);
}
