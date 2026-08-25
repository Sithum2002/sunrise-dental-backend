package com.sunrise.dental.service.impl;

import com.sunrise.dental.constant.AppConstants;
import com.sunrise.dental.entity.Appointment;
import com.sunrise.dental.entity.Bill;
import com.sunrise.dental.enums.ReportFormat;
import com.sunrise.dental.enums.ReportType;
import com.sunrise.dental.exception.ResourceNotFoundException;
import com.sunrise.dental.repository.AppointmentRepository;
import com.sunrise.dental.repository.BillRepository;
import com.sunrise.dental.repository.PatientRepository;
import com.sunrise.dental.repository.TreatmentRepository;
import com.sunrise.dental.service.ReportService;
import com.sunrise.dental.util.AppDateUtils;
import com.sunrise.dental.util.NumberUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleWriterExporterOutput;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JasperReports implementation. Uses the Factory pattern (report type ->
 * template + dataset) and the Template Method style of the Jasper engine.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final AppointmentRepository appointmentRepository;
    private final BillRepository billRepository;
    private final PatientRepository patientRepository;
    private final TreatmentRepository treatmentRepository;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Override
    public GeneratedReport generate(ReportType type, ReportFormat format, Map<String, Object> params) {
        String jrxmlPath = "reports/" + type.name().toLowerCase() + ".jrxml";
        JasperReport jasperReport = loadReport(jrxmlPath);

        ReportData reportData = buildDataset(type, params);
        Map<String, Object> reportParameters = new LinkedHashMap<>(reportData.parameters());
        reportParameters.put("CLINIC_NAME", AppConstants.CLINIC_NAME);
        reportParameters.put("CLINIC_ADDRESS", AppConstants.CLINIC_ADDRESS);
        reportParameters.put("CLINIC_PHONE", AppConstants.CLINIC_PHONE);
        reportParameters.put("CLINIC_EMAIL", AppConstants.CLINIC_EMAIL);
        reportParameters.put("CLINIC_TIN", AppConstants.CLINIC_TIN);
        reportParameters.put("CURRENCY", AppConstants.CURRENCY);
        reportParameters.put("GENERATED_AT", AppDateUtils.formatDateTime(java.time.LocalDateTime.now()));

        return render(jasperReport, reportParameters, reportData.rows(), type, format);
    }

    private JasperReport loadReport(String jrxmlPath) {
        try {
            var resource = new ClassPathResource(jrxmlPath);
            return JasperCompileManager.compileReport(resource.getInputStream());
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load report template " + jrxmlPath + ": " + ex.getMessage(), ex);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private GeneratedReport render(JasperReport jasperReport, Map<String, Object> parameters,
                                   List<Map<String, Object>> rows, ReportType type, ReportFormat format) {
        try {
            JRMapCollectionDataSource dataSource =
                    new JRMapCollectionDataSource((java.util.Collection<Map<String, ?>>) (java.util.Collection) rows);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            if (format == ReportFormat.CSV) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                JRCsvExporter exporter = new JRCsvExporter();
                exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                exporter.setExporterOutput(new SimpleWriterExporterOutput(out, "UTF-8"));
                exporter.exportReport();
                return new GeneratedReport(out.toByteArray(), format.getContentType(),
                        reportFileName(type, format));
            }

            byte[] pdf = JasperExportManager.exportReportToPdf(jasperPrint);
            return new GeneratedReport(pdf, format.getContentType(), reportFileName(type, format));
        } catch (JRException ex) {
            log.error("Report rendering failed for {}: {}", type, ex.getMessage(), ex);
            throw new IllegalStateException("Failed to generate " + type.getLabel() + " report: " + ex.getMessage(), ex);
        }
    }

    private String reportFileName(ReportType type, ReportFormat format) {
        return type.name().toLowerCase() + "_" + java.time.LocalDate.now() + "." + format.getExtension();
    }

    /**
     * Strategy-style dataset builders per report type.
     */
    private ReportData buildDataset(ReportType type, Map<String, Object> params) {
        return switch (type) {
            case PATIENT_BILL -> billReport(params);
            case APPOINTMENT_LIST -> appointmentListReport(params);
            case PATIENT_LIST -> patientListReport(params);
            case PATIENT_TREATMENT_HISTORY -> treatmentHistoryReport(params);
            case REVENUE_SUMMARY -> revenueReport(params);
            case DENTIST_PERFORMANCE -> dentistPerformanceReport(params);
            case TREATMENT_POPULARITY -> treatmentPopularityReport(params);
            case MISSED_APPOINTMENTS -> missedAppointmentsReport(params);
        };
    }

    private ReportData billReport(Map<String, Object> params) {
        Long billId = asLong(params.get("billId"));
        Bill bill = billId != null
                ? billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with id " + billId))
                : billRepository.findByBillNumber(String.valueOf(params.get("billNumber")))
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("billNumber", bill.getBillNumber());
        row.put("billDate", AppDateUtils.formatDateTime(bill.getBilledAt()));
        row.put("appointmentNumber", bill.getAppointment().getAppointmentNumber());
        row.put("patientName", bill.getAppointment().getPatient().getFullName());
        row.put("patientRegNo", bill.getAppointment().getPatient().getRegNo());
        row.put("patientAddress", bill.getAppointment().getPatient().getAddress());
        row.put("patientContact", bill.getAppointment().getPatient().getContactNumber());
        row.put("dentistName", bill.getAppointment().getDentist().getFirstName() + " "
                + bill.getAppointment().getDentist().getLastName());
        row.put("treatmentName", bill.getAppointment().getTreatment().getName());
        row.put("treatmentCode", bill.getAppointment().getTreatment().getCode());
        row.put("consultationFee", NumberUtils.formatCurrency(bill.getConsultationFee()));
        row.put("treatmentCost", NumberUtils.formatCurrency(bill.getTreatmentCost()));
        row.put("discount", NumberUtils.formatCurrency(bill.getDiscount()));
        row.put("tax", NumberUtils.formatCurrency(bill.getTax()));
        row.put("totalAmount", NumberUtils.formatCurrency(bill.getTotalAmount()));
        row.put("amountPaid", NumberUtils.formatCurrency(bill.getAmountPaid()));
        row.put("dueAmount", NumberUtils.formatCurrency(bill.getDueAmount()));
        row.put("paymentStatus", bill.getPaymentStatus().name());
        row.put("paymentMethod", bill.getPaymentMethod() == null ? null : bill.getPaymentMethod().name());

        return new ReportData(List.of(row),
                Map.of("TITLE", "Invoice / Receipt"));
    }

    private ReportData appointmentListReport(Map<String, Object> params) {
        LocalDate from = AppDateUtils.parseDateOrToday(str(params.get("from")));
        LocalDate to = AppDateUtils.parseDateOrToday(str(params.get("to")));
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("'to' date cannot be before 'from' date.");
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        try {
            List<Map<String, Object>> procRows =
                    jdbcTemplate.queryForList("CALL sp_GetAppointmentsByDateRange(?, ?)", from, to);
            for (Map<String, Object> r : procRows) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("appointmentNumber", r.get("appointment_number"));
                row.put("patientName", r.get("patient_name"));
                row.put("patientContact", r.get("patient_contact"));
                row.put("dentistName", r.get("dentist_name"));
                row.put("treatmentName", r.get("treatment"));
                row.put("date", String.valueOf(r.get("appointment_date")));
                row.put("startTime", String.valueOf(r.get("start_time")));
                row.put("status", r.get("status"));
                rows.add(row);
            }
        } catch (Exception ex) {
            appointmentRepository.findByAppointmentDateBetweenOrderByStartTimeAsc(from, to)
                    .forEach(a -> rows.add(appointmentRow(a)));
        }
        return new ReportData(rows, Map.of("TITLE", "Appointment List",
                "PERIOD", AppDateUtils.formatDate(from) + " - " + AppDateUtils.formatDate(to)));
    }

    private ReportData patientListReport(Map<String, Object> params) {
        List<Map<String, Object>> rows = patientRepository.findAll().stream().map(p -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("regNo", p.getRegNo());
            row.put("name", p.getFullName());
            row.put("contact", p.getContactNumber());
            row.put("email", p.getEmail());
            row.put("gender", p.getGender() == null ? null : p.getGender().name());
            row.put("bloodGroup", p.getBloodGroup() == null ? null : p.getBloodGroup().name());
            row.put("createdDate", AppDateUtils.formatDate(p.getCreatedDate().toLocalDate()));
            return row;
        }).toList();
        return new ReportData(rows, Map.of("TITLE", "Patient List",
                "PERIOD", "All registered patients"));
    }

    private ReportData treatmentHistoryReport(Map<String, Object> params) {
        Long patientId = asLong(params.get("patientId"));
        if (patientId == null) {
            throw new ResourceNotFoundException("Patient id is required for the treatment history report");
        }
        var patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id " + patientId));

        List<Map<String, Object>> rows = appointmentRepository
                .findByPatientIdOrderByAppointmentDateDesc(patientId, org.springframework.data.domain.Pageable.unpaged())
                .getContent().stream().map(a -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("appointmentNumber", a.getAppointmentNumber());
                    row.put("date", AppDateUtils.formatDate(a.getAppointmentDate()));
                    row.put("dentistName", a.getDentist().getFirstName() + " " + a.getDentist().getLastName());
                    row.put("treatmentName", a.getTreatment().getName());
                    row.put("status", a.getStatus().name());
                    String billInfo = "-";
                    String amount = "-";
                    String paid = "-";
                    String due = "-";
                    billRepository.findByAppointmentId(a.getId()).ifPresent(b -> {
                        row.put("billNumber", b.getBillNumber());
                        row.put("totalAmount", NumberUtils.formatCurrency(b.getTotalAmount()));
                        row.put("amountPaid", NumberUtils.formatCurrency(b.getAmountPaid()));
                        row.put("dueAmount", NumberUtils.formatCurrency(b.getDueAmount()));
                    });
                    if (!row.containsKey("billNumber")) {
                        row.put("billNumber", billInfo);
                        row.put("totalAmount", amount);
                        row.put("amountPaid", paid);
                        row.put("dueAmount", due);
                    }
                    return row;
                }).toList();

        return new ReportData(rows, Map.of("TITLE", "Patient Treatment History",
                "PATIENT_NAME", patient.getFullName(),
                "PATIENT_REG_NO", patient.getRegNo(),
                "PATIENT_CONTACT", patient.getContactNumber()));
    }

    private ReportData revenueReport(Map<String, Object> params) {
        YearMonth ym = YearMonth.now();
        if (params.get("year") != null && params.get("month") != null) {
            ym = YearMonth.of(Integer.parseInt(String.valueOf(params.get("year"))),
                    Integer.parseInt(String.valueOf(params.get("month"))));
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        try {
            List<Map<String, Object>> procRows =
                    jdbcTemplate.queryForList("CALL sp_GenerateMonthlyRevenue(?, ?)", ym.getYear(), ym.getMonthValue());
            for (Map<String, Object> r : procRows) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("day", r.get("day"));
                row.put("totalBilled", NumberUtils.formatCurrency(toDouble(r.get("total_billed"))));
                row.put("collected", NumberUtils.formatCurrency(toDouble(r.get("collected"))));
                row.put("transactions", r.get("transactions"));
                rows.add(row);
            }
        } catch (Exception ex) {
            LocalDate start = ym.atDay(1);
            LocalDate end = ym.atEndOfMonth();
            billRepository.findByBilledAtBetween(start.atStartOfDay(), end.atTime(java.time.LocalTime.MAX))
                    .forEach(b -> {
                        Map<String, Object> row = new LinkedHashMap<>();
                        row.put("day", b.getBilledAt().getDayOfMonth());
                        row.put("totalBilled", NumberUtils.formatCurrency(b.getTotalAmount()));
                        row.put("collected", NumberUtils.formatCurrency(b.getAmountPaid()));
                        row.put("transactions", 1L);
                        rows.add(row);
                    });
        }
        return new ReportData(rows, Map.of("TITLE", "Revenue Summary",
                "PERIOD", ym.getMonth().name() + " " + ym.getYear()));
    }

    private ReportData dentistPerformanceReport(Map<String, Object> params) {
        LocalDate from = AppDateUtils.parseDateOrToday(str(params.get("from")));
        LocalDate to = AppDateUtils.parseDateOrToday(str(params.get("to")));
        List<Map<String, Object>> rows = new ArrayList<>();
        try {
            List<Map<String, Object>> procRows =
                    jdbcTemplate.queryForList("CALL sp_GetDentistPerformance(?, ?)", from, to);
            for (Map<String, Object> r : procRows) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("dentistName", r.get("dentist_name"));
                row.put("specialization", r.get("specialization"));
                row.put("appointments", asLong(r.get("appointments")));
                row.put("completed", asLong(r.get("completed")));
                row.put("noShows", asLong(r.get("no_shows")));
                row.put("revenue", NumberUtils.formatCurrency(toDouble(r.get("revenue"))));
                row.put("collected", NumberUtils.formatCurrency(toDouble(r.get("collected"))));
                rows.add(row);
            }
        } catch (Exception ex) {
            rows = List.of();
        }
        return new ReportData(rows, Map.of("TITLE", "Dentist Performance",
                "PERIOD", AppDateUtils.formatDate(from) + " - " + AppDateUtils.formatDate(to)));
    }

    private ReportData treatmentPopularityReport(Map<String, Object> params) {
        LocalDate from = AppDateUtils.parseDateOrToday(str(params.get("from")));
        LocalDate to = AppDateUtils.parseDateOrToday(str(params.get("to")));

        Map<Long, long[]> agg = new LinkedHashMap<>();
        Map<Long, String> names = new LinkedHashMap<>();
        appointmentRepository.findByAppointmentDateBetweenOrderByStartTimeAsc(from, to)
                .forEach(a -> {
                    long[] counters = agg.computeIfAbsent(a.getTreatment().getId(), k -> new long[2]);
                    counters[0]++;
                    billRepository.findByAppointmentId(a.getId()).ifPresent(b -> counters[1] += b.getTotalAmount());
                    names.put(a.getTreatment().getId(), a.getTreatment().getName());
                });

        List<Map<String, Object>> rows = agg.entrySet().stream()
                .sorted((x, y) -> Long.compare(y.getValue()[0], x.getValue()[0]))
                .map(e -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("treatmentName", names.get(e.getKey()));
                    row.put("count", e.getValue()[0]);
                    row.put("revenue", NumberUtils.formatCurrency(e.getValue()[1]));
                    return row;
                }).toList();
        return new ReportData(rows, Map.of("TITLE", "Treatment Popularity",
                "PERIOD", AppDateUtils.formatDate(from) + " - " + AppDateUtils.formatDate(to)));
    }

    private ReportData missedAppointmentsReport(Map<String, Object> params) {
        LocalDate from = AppDateUtils.parseDateOrToday(str(params.get("from")));
        LocalDate to = AppDateUtils.parseDateOrToday(str(params.get("to")));

        List<Map<String, Object>> rows = appointmentRepository
                .findByAppointmentDateBetweenOrderByStartTimeAsc(from, to).stream()
                .filter(a -> a.getStatus() == com.sunrise.dental.enums.AppointmentStatus.NO_SHOW
                        || a.getStatus() == com.sunrise.dental.enums.AppointmentStatus.CANCELLED)
                .map(a -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("appointmentNumber", a.getAppointmentNumber());
                    row.put("patientName", a.getPatient().getFullName());
                    row.put("patientContact", a.getPatient().getContactNumber());
                    row.put("dentistName", a.getDentist().getFirstName() + " " + a.getDentist().getLastName());
                    row.put("date", AppDateUtils.formatDate(a.getAppointmentDate()));
                    row.put("time", String.valueOf(a.getStartTime()));
                    row.put("status", a.getStatus().name());
                    return row;
                }).toList();
        return new ReportData(rows, Map.of("TITLE", "Missed / No-show Appointments",
                "PERIOD", AppDateUtils.formatDate(from) + " - " + AppDateUtils.formatDate(to)));
    }

    private Map<String, Object> appointmentRow(Appointment a) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("appointmentNumber", a.getAppointmentNumber());
        row.put("patientName", a.getPatient().getFullName());
        row.put("patientContact", a.getPatient().getContactNumber());
        row.put("dentistName", a.getDentist().getFirstName() + " " + a.getDentist().getLastName());
        row.put("treatmentName", a.getTreatment().getName());
        row.put("date", AppDateUtils.formatDate(a.getAppointmentDate()));
        row.put("startTime", String.valueOf(a.getStartTime()));
        row.put("status", a.getStatus());
        return row;
    }

    private double toDouble(Object value) {
        if (value == null) {
            return 0.0;
        }
        return ((Number) value).doubleValue();
    }

    private Long asLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number n) {
            return n.longValue();
        }
        String s = String.valueOf(value).trim();
        if (s.indexOf('.') >= 0) {
            s = s.substring(0, s.indexOf('.'));
        }
        return Long.parseLong(s);
    }

    private String str(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private record ReportData(List<Map<String, Object>> rows, Map<String, Object> parameters) {
    }
}
