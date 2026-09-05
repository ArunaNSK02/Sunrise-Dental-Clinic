package com.sunrisedental.service;

import com.sunrisedental.dao.NotificationDAO;
import com.sunrisedental.dao.impl.NotificationDAOImpl;
import com.sunrisedental.model.Appointment;
import com.sunrisedental.model.Notification;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Business tier for the notification feature. Decides <i>when</i> and
 * <i>what</i> to send; {@link NotificationChannel} (Strategy pattern)
 * decides <i>how</i>.
 *
 * <p>A notification failure must never fail the booking/cancel/
 * reschedule it's attached to — a text message not going out is not a
 * reason to lose an appointment record. Callers should treat this class
 * as best-effort and keep going regardless of what it returns.</p>
 */
public class NotificationService {

    private static final Logger LOG = Logger.getLogger(NotificationService.class.getName());
    // LocalDate-only pattern — a bare "dd MMM yyyy 'at' HH:mm" would throw
    // UnsupportedTemporalTypeException formatting a LocalDate (no time
    // field of its own), which is why the time is appended separately.
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);

    private final NotificationChannel channel;
    private final NotificationDAO notificationDAO;

    public NotificationService() {
        this(new SmsNotificationChannel(), new NotificationDAOImpl());
    }

    public NotificationService(NotificationChannel channel, NotificationDAO notificationDAO) {
        this.channel = channel;
        this.notificationDAO = notificationDAO;
    }

    public void notifyAppointmentBooked(Appointment appointment) {
        String message = "Sunrise Dental Clinic: your appointment (#" + appointment.getAppointmentNumber()
                + ") with " + appointment.getDentist().getFullName() + " for " + appointment.getTreatment().getName()
                + " is confirmed for " + appointment.getDate().format(DATE_FORMAT)
                + " at " + appointment.getTime() + ".";
        notify(appointment, message);
    }

    public void notifyAppointmentCancelled(Appointment appointment) {
        String message = "Sunrise Dental Clinic: your appointment (#" + appointment.getAppointmentNumber()
                + ") on " + appointment.getDate() + " has been cancelled.";
        notify(appointment, message);
    }

    public void notifyAppointmentRescheduled(Appointment appointment) {
        String message = "Sunrise Dental Clinic: your appointment (#" + appointment.getAppointmentNumber()
                + ") has been rescheduled to " + appointment.getDate() + " " + appointment.getTime() + ".";
        notify(appointment, message);
    }

    private void notify(Appointment appointment, String message) {
        try {
            String recipient = appointment.getPatient().getContactNumber();
            boolean sent = channel.send(recipient, message);
            if (sent) {
                Notification record = new Notification(
                        appointment.getAppointmentNumber(), appointment.getPatient().getPatientId(),
                        channel.name(), recipient, message);
                notificationDAO.save(record);
            }
        } catch (RuntimeException e) {
            // Deliberately swallowed (logged, not rethrown) — see class
            // javadoc: a notification problem must never fail the
            // appointment operation it's attached to.
            LOG.log(Level.WARNING, "Notification failed for appointment "
                    + appointment.getAppointmentNumber() + " — booking/change still succeeded.", e);
        }
    }
}
