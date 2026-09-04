-- Sample data for local development/demo — NOT run automatically, load
-- it yourself when you want data to click through the app with:
--   mysql -u root -p sunrise_dental < src/main/resources/seed-data.sql
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
