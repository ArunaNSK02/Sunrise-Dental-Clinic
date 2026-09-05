-- Sunrise Dental Clinic — MySQL schema (Task B data access tier)
--
-- Deliberately a first-pass schema to unblock the DAO implementations:
-- no stored procedures/triggers yet (e.g. a DB-level double-booking
-- guard) — those are a planned follow-up per the marking rubric's
-- "advanced DB features" criterion, not yet built. Run manually against
-- a local `sunrise_dental` database:
--   mysql -u root -p -e "CREATE DATABASE sunrise_dental"
--   mysql -u root -p sunrise_dental < src/main/resources/schema.sql

CREATE TABLE IF NOT EXISTS users (
    user_id    INT AUTO_INCREMENT PRIMARY KEY,
    username   VARCHAR(50)  NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    full_name  VARCHAR(100) NOT NULL,
    -- Discriminator column: which User subclass this row represents.
    role       ENUM('RECEPTIONIST', 'ADMINISTRATOR', 'DENTIST') NOT NULL
);

-- One row per Dentist, extending the shared users row (decision 15:
-- Dentist extends User directly, holding dentist-only fields here).
CREATE TABLE IF NOT EXISTS dentists (
    dentist_id               INT PRIMARY KEY,
    daily_appointment_limit  INT NOT NULL DEFAULT 20,
    FOREIGN KEY (dentist_id) REFERENCES users(user_id)
);

-- Composition: Dentist *-- AvailabilityBlock (decision 19) — cascades on
-- delete since a block has no meaning outside its dentist.
CREATE TABLE IF NOT EXISTS availability_blocks (
    block_id        INT AUTO_INCREMENT PRIMARY KEY,
    dentist_id      INT NOT NULL,
    start_datetime  DATETIME NOT NULL,
    end_datetime    DATETIME NOT NULL,
    reason          VARCHAR(255),
    FOREIGN KEY (dentist_id) REFERENCES dentists(dentist_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS patients (
    patient_id      INT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    address         VARCHAR(255),
    contact_number  VARCHAR(20)  NOT NULL
);

-- duration_minutes added per Class Diagram rev. 2 / decision 24.
CREATE TABLE IF NOT EXISTS treatments (
    treatment_id      INT AUTO_INCREMENT PRIMARY KEY,
    name              VARCHAR(100)   NOT NULL,
    cost              DECIMAL(10, 2) NOT NULL,
    duration_minutes  INT            NOT NULL
);

-- The central table (decision 20: everything else navigates toward it).
CREATE TABLE IF NOT EXISTS appointments (
    appointment_number  INT AUTO_INCREMENT PRIMARY KEY,
    patient_id          INT  NOT NULL,
    dentist_id          INT  NOT NULL,
    treatment_id        INT  NOT NULL,
    appointment_date    DATE NOT NULL,
    appointment_time    TIME NOT NULL,
    status              ENUM('SCHEDULED', 'DELAYED', 'CANCELLED', 'RESCHEDULED', 'COMPLETED')
                             NOT NULL DEFAULT 'SCHEDULED',
    change_reason       ENUM('PATIENT', 'DENTIST'),
    delay_minutes       INT  NOT NULL DEFAULT 0,
    FOREIGN KEY (patient_id)   REFERENCES patients(patient_id),
    FOREIGN KEY (dentist_id)   REFERENCES dentists(dentist_id),
    FOREIGN KEY (treatment_id) REFERENCES treatments(treatment_id)
);

-- Composition: Appointment *-- Bill (decision 19), 1-to-0..1.
CREATE TABLE IF NOT EXISTS bills (
    bill_id             INT AUTO_INCREMENT PRIMARY KEY,
    appointment_number  INT UNIQUE NOT NULL,
    treatment_cost      DECIMAL(10, 2) NOT NULL,
    consultation_fee    DECIMAL(10, 2) NOT NULL,
    total_amount        DECIMAL(10, 2) NOT NULL,
    issue_date          DATE NOT NULL,
    FOREIGN KEY (appointment_number) REFERENCES appointments(appointment_number) ON DELETE CASCADE
);

-- Single-row config table, not a class-diagram entity (decision 27):
-- backs the flat clinic-wide consultation fee (decision 26). id is
-- always 1 — there is exactly one row, enforced by the primary key
-- rather than a separate "is this the active row" flag.
CREATE TABLE IF NOT EXISTS clinic_settings (
    id                INT PRIMARY KEY DEFAULT 1,
    consultation_fee  DECIMAL(10, 2) NOT NULL
);
INSERT IGNORE INTO clinic_settings (id, consultation_fee) VALUES (1, 1000.00);

-- Notification audit trail (rubric: "complex functionality — e.g. email
-- alerts, SMS notifications"). appointment_number is nullable and has no
-- FK/cascade: a notification is a record that something was sent, which
-- should survive even if the appointment it was about is later deleted
-- (appointments never actually get hard-deleted by this app, but the
-- audit trail shouldn't silently vanish if that ever changed).
CREATE TABLE IF NOT EXISTS notifications (
    notification_id     INT AUTO_INCREMENT PRIMARY KEY,
    appointment_number   INT,
    patient_id           INT NOT NULL,
    channel              VARCHAR(20) NOT NULL,
    recipient            VARCHAR(50) NOT NULL,
    message              VARCHAR(500) NOT NULL,
    sent_at              DATETIME NOT NULL,
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id)
);

-- ============================================================
-- Advanced DB features (rubric: stored procedures/functions/triggers
-- enforcing business rules) — defense in depth alongside the
-- application-level checks in AppointmentService/AppointmentDAOImpl
-- (decision 24, entries 61-63): the Java layer is the primary,
-- user-facing check (it can show a proper "unavailable" message
-- instead of a raw SQL error); these exist so the double-booking rule
-- holds even against a write that bypasses the DAO layer entirely
-- (a raw SQL script, another application, a DBA typo).
-- ============================================================

DELIMITER $$

-- Trigger 1/2: block an INSERT that would double-book a dentist.
-- Mirrors AppointmentDAOImpl.hasClash()'s overlap test in SQL:
-- [existingStart, existingStart+existingDuration) overlaps
-- [NEW.time, NEW.time+NEWDuration) exactly when existingStart <
-- NEW.end AND NEW.time < existingEnd.
DROP TRIGGER IF EXISTS trg_appointments_no_double_booking_insert$$
CREATE TRIGGER trg_appointments_no_double_booking_insert
BEFORE INSERT ON appointments
FOR EACH ROW
BEGIN
    DECLARE v_conflicts INT;
    IF NEW.status <> 'CANCELLED' THEN
        SELECT COUNT(*) INTO v_conflicts
        FROM appointments a
        JOIN treatments t ON t.treatment_id = a.treatment_id
        JOIN treatments nt ON nt.treatment_id = NEW.treatment_id
        WHERE a.dentist_id = NEW.dentist_id
          AND a.appointment_date = NEW.appointment_date
          AND a.status <> 'CANCELLED'
          AND a.appointment_time < ADDTIME(NEW.appointment_time, SEC_TO_TIME(nt.duration_minutes * 60))
          AND NEW.appointment_time < ADDTIME(a.appointment_time, SEC_TO_TIME(t.duration_minutes * 60));

        IF v_conflicts > 0 THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Double-booking guard: this dentist already has an overlapping appointment at that time.';
        END IF;
    END IF;
END$$

-- Trigger 2/2: same guard for an UPDATE that moves an appointment
-- (Reschedule Appointment) — only runs the check when the date/time/
-- dentist actually changed, and excludes the row's own current slot
-- from the conflict search (the Java-side equivalent is decision 28's
-- excludeAppointmentNumber parameter).
DROP TRIGGER IF EXISTS trg_appointments_no_double_booking_update$$
CREATE TRIGGER trg_appointments_no_double_booking_update
BEFORE UPDATE ON appointments
FOR EACH ROW
BEGIN
    DECLARE v_conflicts INT;
    IF NEW.status <> 'CANCELLED'
       AND (NEW.appointment_date <> OLD.appointment_date
            OR NEW.appointment_time <> OLD.appointment_time
            OR NEW.dentist_id <> OLD.dentist_id) THEN
        SELECT COUNT(*) INTO v_conflicts
        FROM appointments a
        JOIN treatments t ON t.treatment_id = a.treatment_id
        JOIN treatments nt ON nt.treatment_id = NEW.treatment_id
        WHERE a.dentist_id = NEW.dentist_id
          AND a.appointment_date = NEW.appointment_date
          AND a.status <> 'CANCELLED'
          AND a.appointment_number <> NEW.appointment_number
          AND a.appointment_time < ADDTIME(NEW.appointment_time, SEC_TO_TIME(nt.duration_minutes * 60))
          AND NEW.appointment_time < ADDTIME(a.appointment_time, SEC_TO_TIME(t.duration_minutes * 60));

        IF v_conflicts > 0 THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Double-booking guard: this dentist already has an overlapping appointment at that time.';
        END IF;
    END IF;
END$$

-- Function: total = treatment cost + consultation fee (the brief's core
-- billing rule, decision 26), callable directly from SQL. BillDAOImpl
-- uses this in its INSERT rather than only computing the total in Java,
-- so the rule is enforced at both tiers, not duplicated by coincidence.
DROP FUNCTION IF EXISTS fn_bill_total$$
CREATE FUNCTION fn_bill_total(p_treatment_cost DECIMAL(10,2), p_consultation_fee DECIMAL(10,2))
RETURNS DECIMAL(10,2)
DETERMINISTIC
BEGIN
    RETURN p_treatment_cost + p_consultation_fee;
END$$

-- Stored procedure: revenue billed for one dentist on one date — backs
-- an extra column on View Reports' per-dentist load table (Administrator
-- use case, decision 5), called via a JDBC CallableStatement from
-- ReportDAOImpl rather than duplicated as another aggregate SELECT in
-- Java, to actually demonstrate a procedure in use, not just present.
DROP PROCEDURE IF EXISTS sp_dentist_daily_revenue$$
CREATE PROCEDURE sp_dentist_daily_revenue(IN p_dentist_id INT, IN p_date DATE, OUT p_revenue DECIMAL(10,2))
BEGIN
    SELECT COALESCE(SUM(b.total_amount), 0) INTO p_revenue
    FROM bills b
    JOIN appointments a ON a.appointment_number = b.appointment_number
    WHERE a.dentist_id = p_dentist_id AND a.appointment_date = p_date;
END$$

DELIMITER ;
