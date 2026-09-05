-- Sample data for local development/demo — NOT run automatically, load
-- it yourself when you want data to click through the app with:
--   mysql -u root -p sunrise_dental < src/main/resources/seed-data.sql
--
-- One-time load against a fresh schema — none of this is idempotent
-- (no INSERT IGNORE/ON DUPLICATE), matching the rest of this file's
-- existing style. Re-running it will duplicate the patients/appointments/
-- treatments below (users will fail loudly instead, since username is
-- UNIQUE) — clear those tables first if you need to reload.
--
-- Passwords are real BCrypt hashes (UserService.verifyPassword() uses
-- BCrypt.checkpw() — plain-text comparison was a placeholder, now
-- replaced) generated once via jshell + jbcrypt, not typed by hand:
--   reception -> reception123
--   admin     -> admin123
--   d.perera / d.silva -> dentist123
-- Login with the plain-text password on the right; the hash below is
-- what's actually stored.

INSERT INTO users (username, password, full_name, role) VALUES
    ('reception', '$2a$10$wP3qaB3V3iHPRn0WcEOOw.46M1No.B9BeWrZ6f0pZDHUSFyz18ExW', 'Nadeesha Fernando', 'RECEPTIONIST'),
    ('admin', '$2a$10$V3gfbu.6i8GPNKtmHNdQ3.KpR.nQN/kaE6/l19yWl4e6Q6CTTHpK.', 'Priya Jayasuriya', 'ADMINISTRATOR'),
    ('d.perera', '$2a$10$bN2gK4kqWoi4r1.QAvWrUuF8hel0fjuw3ZWVjt/1Uoj6UQo0lptlS', 'Dr. Kasun Perera', 'DENTIST'),
    ('d.silva', '$2a$10$bN2gK4kqWoi4r1.QAvWrUuF8hel0fjuw3ZWVjt/1Uoj6UQo0lptlS', 'Dr. Amaya Silva', 'DENTIST');

-- dentist_id = the user_id MySQL just assigned each DENTIST row above.
-- Adjust these two numbers if you already had other users in the table
-- before running this script (SELECT user_id FROM users WHERE role =
-- 'DENTIST' to check).
INSERT INTO dentists (dentist_id, daily_appointment_limit)
    SELECT user_id, 16 FROM users WHERE username = 'd.perera';
INSERT INTO dentists (dentist_id, daily_appointment_limit)
    SELECT user_id, 12 FROM users WHERE username = 'd.silva';

INSERT INTO treatments (name, cost, duration_minutes) VALUES
    ('Routine Checkup', 2500.00, 20),
    ('Scaling & Polishing', 4500.00, 30),
    ('Filling', 6000.00, 40),
    ('Root Canal Treatment', 18000.00, 90),
    ('Tooth Extraction', 8000.00, 30);

-- ============================================================
-- Patients + appointments spanning yesterday/today/next week, across
-- both dentists, with varied statuses — so logging in as each of the
-- three roles has something real to look at (a receptionist/admin
-- Dashboard's "Today's Schedule", a dentist's own schedule, and View
-- Reports' status counts/revenue/per-dentist load all show non-empty,
-- realistic data instead of "nothing scheduled"). Appointment times are
-- deliberately spaced so no two same-dentist rows below overlap — the
-- schema's double-booking trigger (trg_appointments_no_double_booking_insert)
-- would otherwise reject the second one.
-- ============================================================

SELECT user_id INTO @dentist_perera FROM users WHERE username = 'd.perera';
SELECT user_id INTO @dentist_silva FROM users WHERE username = 'd.silva';
SELECT treatment_id INTO @t_checkup FROM treatments WHERE name = 'Routine Checkup';
SELECT treatment_id INTO @t_scaling FROM treatments WHERE name = 'Scaling & Polishing';
SELECT treatment_id INTO @t_filling FROM treatments WHERE name = 'Filling';
SELECT treatment_id INTO @t_root_canal FROM treatments WHERE name = 'Root Canal Treatment';
SELECT treatment_id INTO @t_extraction FROM treatments WHERE name = 'Tooth Extraction';

INSERT INTO patients (name, address, contact_number) VALUES
    ('Saman Kumara', '12 Galle Road, Colombo 03', '0771112233'),
    ('Dilani Perera', '45 Kandy Road, Kadawatha', '0772223344'),
    ('Ruwan Silva', '8 Negombo Road, Wattala', '0773334455'),
    ('Anusha Fernando', '22 High Level Road, Nugegoda', '0774445566'),
    ('Chamara Bandara', '5 Baseline Road, Colombo 09', '0775556677');

SELECT patient_id INTO @p_saman FROM patients WHERE contact_number = '0771112233';
SELECT patient_id INTO @p_dilani FROM patients WHERE contact_number = '0772223344';
SELECT patient_id INTO @p_ruwan FROM patients WHERE contact_number = '0773334455';
SELECT patient_id INTO @p_anusha FROM patients WHERE contact_number = '0774445566';
SELECT patient_id INTO @p_chamara FROM patients WHERE contact_number = '0775556677';

-- Yesterday: one completed visit (feeds View Reports' revenue via the
-- bill below) and one the patient cancelled.
INSERT INTO appointments (patient_id, dentist_id, treatment_id, appointment_date, appointment_time, status, change_reason, delay_minutes) VALUES
    (@p_saman, @dentist_perera, @t_checkup, DATE_SUB(CURDATE(), INTERVAL 1 DAY), '09:00:00', 'COMPLETED', NULL, 0),
    (@p_dilani, @dentist_silva, @t_scaling, DATE_SUB(CURDATE(), INTERVAL 1 DAY), '10:00:00', 'CANCELLED', 'PATIENT', 0);

-- Today: a realistic spread across both dentists, including a
-- dentist-caused delay example (decision 10's cascade — this row is
-- what it looks like after the cascade already ran).
INSERT INTO appointments (patient_id, dentist_id, treatment_id, appointment_date, appointment_time, status, change_reason, delay_minutes) VALUES
    (@p_ruwan, @dentist_perera, @t_filling, CURDATE(), '09:30:00', 'SCHEDULED', NULL, 0),
    (@p_anusha, @dentist_perera, @t_checkup, CURDATE(), '11:00:00', 'DELAYED', 'DENTIST', 20),
    (@p_chamara, @dentist_silva, @t_extraction, CURDATE(), '14:00:00', 'SCHEDULED', NULL, 0);

-- Tomorrow / next week: future bookings, including one already rescheduled.
INSERT INTO appointments (patient_id, dentist_id, treatment_id, appointment_date, appointment_time, status, change_reason, delay_minutes) VALUES
    (@p_saman, @dentist_silva, @t_root_canal, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '10:00:00', 'SCHEDULED', NULL, 0),
    (@p_dilani, @dentist_perera, @t_scaling, DATE_ADD(CURDATE(), INTERVAL 7 DAY), '09:00:00', 'RESCHEDULED', 'PATIENT', 0);

-- A bill for yesterday's completed appointment, computed the same way
-- BillDAOImpl.save() does (fn_bill_total), so View Reports shows real revenue.
SELECT appointment_number INTO @appt_completed FROM appointments
    WHERE patient_id = @p_saman AND appointment_date = DATE_SUB(CURDATE(), INTERVAL 1 DAY) LIMIT 1;
INSERT INTO bills (appointment_number, treatment_cost, consultation_fee, total_amount, issue_date)
    SELECT @appt_completed, t.cost, 1000.00, fn_bill_total(t.cost, 1000.00), DATE_SUB(CURDATE(), INTERVAL 1 DAY)
    FROM treatments t WHERE t.treatment_id = @t_checkup;
