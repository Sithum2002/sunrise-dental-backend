package com.sunrise.dental.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * Creates the advanced MySQL database objects - stored procedures, functions
 * and triggers that implement business rules at the database level
 * (3-tier architecture, data-tier business logic). Idempotent and safe to
 * re-run on every startup. Skipped automatically when the database is not MySQL
 * (e.g. H2 during unit tests).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DbProcedureInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    @Override
    public void run(String... args) {
        if (!isMySql()) {
            log.info("Skipping MySQL stored procedures/triggers initialisation (non-MySQL database).");
            return;
        }
        createAuditTable();
        createStoredProcedures();
        createFunctions();
        createTriggers();
        log.info("MySQL stored procedures, functions and triggers initialised successfully.");
    }

    private boolean isMySql() {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData meta = connection.getMetaData();
            String product = meta.getDatabaseProductName();
            return product != null && product.toLowerCase().contains("mysql");
        } catch (SQLException ex) {
            log.warn("Unable to detect database product: {}", ex.getMessage());
            return false;
        }
    }

    private void createAuditTable() {
        execute("""
                CREATE TABLE IF NOT EXISTS appointment_audit_log (
                    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
                    appointment_id BIGINT       NOT NULL,
                    action        VARCHAR(20)   NOT NULL,
                    old_status    VARCHAR(20)   NULL,
                    new_status    VARCHAR(20)   NULL,
                    old_date      DATE          NULL,
                    new_date      DATE          NULL,
                    changed_by    VARCHAR(100)  NULL,
                    changed_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """);
    }

    private void createStoredProcedures() {
        execute("DROP PROCEDURE IF EXISTS sp_GenerateMonthlyRevenue");
        execute("""
                CREATE PROCEDURE sp_GenerateMonthlyRevenue(IN p_year INT, IN p_month INT)
                BEGIN
                    SELECT DAY(b.billed_at)          AS day,
                           SUM(b.total_amount)       AS total_billed,
                           SUM(b.amount_paid)        AS collected,
                           COUNT(*)                  AS transactions
                    FROM bills b
                    WHERE YEAR(b.billed_at)  = p_year
                      AND MONTH(b.billed_at) = p_month
                    GROUP BY DAY(b.billed_at)
                    ORDER BY day;
                END
                """);

        execute("DROP PROCEDURE IF EXISTS sp_GetAppointmentsByDateRange");
        execute("""
                CREATE PROCEDURE sp_GetAppointmentsByDateRange(IN p_start_date DATE, IN p_end_date DATE)
                BEGIN
                    SELECT a.appointment_number,
                           CONCAT(p.first_name, ' ', p.last_name)  AS patient_name,
                           p.contact_number                         AS patient_contact,
                           CONCAT(d.first_name, ' ', d.last_name)   AS dentist_name,
                           t.name                                   AS treatment,
                           a.appointment_date,
                           a.start_time,
                           a.end_time,
                           a.status
                    FROM appointments a
                    JOIN patients   p ON a.patient_id   = p.id
                    JOIN dentists   d ON a.dentist_id   = d.id
                    JOIN treatments t ON a.treatment_id = t.id
                    WHERE a.appointment_date BETWEEN p_start_date AND p_end_date
                    ORDER BY a.appointment_date, a.start_time;
                END
                """);

        execute("DROP PROCEDURE IF EXISTS sp_GetDentistPerformance");
        execute("""
                CREATE PROCEDURE sp_GetDentistPerformance(IN p_from DATE, IN p_to DATE)
                BEGIN
                    SELECT d.id                                        AS dentist_id,
                           CONCAT(d.first_name, ' ', d.last_name)     AS dentist_name,
                           d.specialization,
                           COUNT(a.id)                                 AS appointments,
                           SUM(CASE WHEN a.status = 'COMPLETED'
                                    THEN 1 ELSE 0 END)                 AS completed,
                           SUM(CASE WHEN a.status = 'NO_SHOW'
                                    THEN 1 ELSE 0 END)                 AS no_shows,
                           COALESCE(SUM(b.total_amount), 0)            AS revenue,
                           COALESCE(SUM(b.amount_paid), 0)             AS collected
                    FROM dentists d
                    LEFT JOIN appointments a ON a.dentist_id = d.id
                           AND a.appointment_date BETWEEN p_from AND p_to
                    LEFT JOIN bills b ON b.appointment_id = a.id
                    GROUP BY d.id, d.first_name, d.last_name, d.specialization
                    ORDER BY revenue DESC;
                END
                """);
    }

    private void createFunctions() {
        execute("DROP FUNCTION IF EXISTS fn_GetPatientVisitCount");
        execute("""
                CREATE FUNCTION fn_GetPatientVisitCount(p_patient_id BIGINT) RETURNS INT DETERMINISTIC
                BEGIN
                    DECLARE v_count INT;
                    SELECT COUNT(*) INTO v_count
                    FROM appointments
                    WHERE patient_id = p_patient_id
                      AND status <> 'CANCELLED';
                    RETURN v_count;
                END
                """);
    }

    private void createTriggers() {
        execute("DROP TRIGGER IF EXISTS trg_appointment_business_hours");
        execute("""
                CREATE TRIGGER trg_appointment_business_hours
                BEFORE INSERT ON appointments
                FOR EACH ROW
                BEGIN
                    IF NEW.start_time < '08:00:00' OR NEW.start_time > '17:30:00' THEN
                        SIGNAL SQLSTATE '45000'
                        SET MESSAGE_TEXT = 'Appointments must be booked between 08:00 and 17:30';
                    END IF;
                    IF NEW.appointment_date IS NULL THEN
                        SIGNAL SQLSTATE '45000'
                        SET MESSAGE_TEXT = 'Appointment date is required';
                    END IF;
                END
                """);

        execute("DROP TRIGGER IF EXISTS trg_appointment_insert_audit");
        execute("""
                CREATE TRIGGER trg_appointment_insert_audit
                AFTER INSERT ON appointments
                FOR EACH ROW
                BEGIN
                    INSERT INTO appointment_audit_log (appointment_id, action, new_status, new_date, changed_by)
                    VALUES (NEW.id, 'INSERT', NEW.status, NEW.appointment_date,
                            COALESCE(@current_user, 'system'));
                END
                """);

        execute("DROP TRIGGER IF EXISTS trg_appointment_update_audit");
        execute("""
                CREATE TRIGGER trg_appointment_update_audit
                AFTER UPDATE ON appointments
                FOR EACH ROW
                BEGIN
                    INSERT INTO appointment_audit_log (appointment_id, action, old_status, new_status,
                                                       old_date, new_date, changed_by)
                    VALUES (NEW.id, 'UPDATE', OLD.status, NEW.status,
                            OLD.appointment_date, NEW.appointment_date,
                            COALESCE(@current_user, 'system'));
                END
                """);

        execute("DROP TRIGGER IF EXISTS trg_appointment_delete_audit");
        execute("""
                CREATE TRIGGER trg_appointment_delete_audit
                AFTER DELETE ON appointments
                FOR EACH ROW
                BEGIN
                    INSERT INTO appointment_audit_log (appointment_id, action, old_status, old_date, changed_by)
                    VALUES (OLD.id, 'DELETE', OLD.status, OLD.appointment_date,
                            COALESCE(@current_user, 'system'));
                END
                """);
    }

    private void execute(String sql) {
        jdbcTemplate.execute(sql);
    }
}
