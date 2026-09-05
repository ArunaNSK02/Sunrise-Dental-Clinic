package com.sunrisedental.service;

import com.sunrisedental.dao.NotificationDAO;
import com.sunrisedental.model.Appointment;
import com.sunrisedental.model.Dentist;
import com.sunrisedental.model.Notification;
import com.sunrisedental.model.Patient;
import com.sunrisedental.model.Treatment;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the notification feature (Strategy pattern:
 * NotificationService depends only on the NotificationChannel interface,
 * never a concrete delivery mechanism).
 */
class NotificationServiceTest {

    private final Appointment appointment = new Appointment(
            42, new Patient(1, "Jane Silva", "1 Galle Rd", "0771234567"),
            new Dentist(1, "d.perera", "x", "Dr. Perera", 1, 20),
            new Treatment(1, "Filling", 4000.0, 30),
            LocalDate.of(2026, 9, 10), LocalTime.of(10, 0));

    private static class StubChannel implements NotificationChannel {
        final boolean succeeds;
        final List<String> sentTo = new ArrayList<>();
        final List<String> sentMessages = new ArrayList<>();

        StubChannel(boolean succeeds) {
            this.succeeds = succeeds;
        }

        @Override
        public boolean send(String recipient, String message) {
            sentTo.add(recipient);
            sentMessages.add(message);
            return succeeds;
        }

        @Override
        public String name() {
            return "STUB";
        }
    }

    private static class ThrowingChannel implements NotificationChannel {
        @Override
        public boolean send(String recipient, String message) {
            throw new RuntimeException("simulated gateway outage");
        }

        @Override
        public String name() {
            return "STUB";
        }
    }

    private static class StubNotificationDAO implements NotificationDAO {
        final List<Notification> saved = new ArrayList<>();

        @Override
        public Notification save(Notification notification) {
            saved.add(notification);
            return notification;
        }

        @Override
        public List<Notification> findByAppointmentNumber(int appointmentNumber) {
            return saved;
        }
    }

    @Test
    void notifyAppointmentBooked_sendsToThePatientsContactNumberAndPersistsARecord() {
        StubChannel channel = new StubChannel(true);
        StubNotificationDAO dao = new StubNotificationDAO();
        NotificationService service = new NotificationService(channel, dao);

        service.notifyAppointmentBooked(appointment);

        assertEquals(1, channel.sentTo.size());
        assertEquals("0771234567", channel.sentTo.get(0));
        assertTrue(channel.sentMessages.get(0).contains("42"), "message should reference the appointment number");
        assertEquals(1, dao.saved.size());
        assertEquals(42, dao.saved.get(0).getAppointmentNumber());
    }

    @Test
    void notify_doesNotPersistWhenTheChannelReportsFailure() {
        StubChannel channel = new StubChannel(false);
        StubNotificationDAO dao = new StubNotificationDAO();
        NotificationService service = new NotificationService(channel, dao);

        service.notifyAppointmentCancelled(appointment);

        assertTrue(dao.saved.isEmpty());
    }

    @Test
    void notify_neverThrowsEvenWhenTheChannelDoes() {
        StubNotificationDAO dao = new StubNotificationDAO();
        NotificationService service = new NotificationService(new ThrowingChannel(), dao);

        // A notification failure must never fail the appointment operation
        // it's attached to (class javadoc) — this must not throw.
        assertDoesNotThrow(() -> service.notifyAppointmentRescheduled(appointment));
    }
}
