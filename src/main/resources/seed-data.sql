-- Sample data for local development/demo — NOT run automatically, load
-- it yourself when you want data to click through the app with:
--   mysql -u root -p sunrise_dental < src/main/resources/seed-data.sql
--
-- Passwords are plain text here because UserService.verifyPassword()
-- still does plain-text comparison (a known placeholder — see DESIGN.md
-- decision 26's neighbouring note, and the report's security
-- discussion). Never do this against real patient data.

INSERT INTO users (username, password, full_name, role) VALUES
    ('reception', 'reception123', 'Nadeesha Fernando', 'RECEPTIONIST'),
    ('admin', 'admin123', 'Priya Jayasuriya', 'ADMINISTRATOR'),
    ('d.perera', 'dentist123', 'Dr. Kasun Perera', 'DENTIST'),
    ('d.silva', 'dentist123', 'Dr. Amaya Silva', 'DENTIST');

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
