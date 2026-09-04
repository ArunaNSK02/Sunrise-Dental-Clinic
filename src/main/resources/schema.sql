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
