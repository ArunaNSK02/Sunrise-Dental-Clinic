package com.sunrisedental.dao;

import com.sunrisedental.model.Notification;

import java.util.List;

/** Persists the notification audit trail — see {@link Notification}'s class javadoc. */
public interface NotificationDAO {

    Notification save(Notification notification);

    List<Notification> findByAppointmentNumber(int appointmentNumber);
}
