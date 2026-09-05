package com.sunrisedental.model;

import java.time.LocalDateTime;

/**
 * A record that a notification was sent to a patient (rubric: "complex
 * functionality — e.g. email alerts, SMS notifications"). Not on the
 * class diagram — no use case in Task A creates or reads notifications,
 * same reasoning as decision 27's {@code clinic_settings} — but kept as
 * a real, persisted audit trail rather than only a log line, so staff
 * can see what was (attempted to be) sent.
 */
public class Notification {

    private int notificationId;
    private Integer appointmentNumber;
    private int patientId;
    private String channel;
    private String recipient;
    private String message;
    private LocalDateTime sentAt;

    public Notification() {
    }

    public Notification(Integer appointmentNumber, int patientId, String channel, String recipient, String message) {
        this.appointmentNumber = appointmentNumber;
        this.patientId = patientId;
        this.channel = channel;
        this.recipient = recipient;
        this.message = message;
        this.sentAt = LocalDateTime.now();
    }

    public int getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    public Integer getAppointmentNumber() {
        return appointmentNumber;
    }

    public void setAppointmentNumber(Integer appointmentNumber) {
        this.appointmentNumber = appointmentNumber;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
}
