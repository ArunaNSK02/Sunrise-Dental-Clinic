package com.sunrisedental.dao.impl;

import com.sunrisedental.dao.NotificationDAO;
import com.sunrisedental.db.DBConnectionManager;
import com.sunrisedental.model.Notification;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAOImpl implements NotificationDAO {

    @Override
    public Notification save(Notification notification) {
        String sql = "INSERT INTO notifications (appointment_number, patient_id, channel, recipient, message, sent_at) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setObject(1, notification.getAppointmentNumber());
            stmt.setInt(2, notification.getPatientId());
            stmt.setString(3, notification.getChannel());
            stmt.setString(4, notification.getRecipient());
            stmt.setString(5, notification.getMessage());
            stmt.setObject(6, notification.getSentAt());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    notification.setNotificationId(keys.getInt(1));
                }
            }
            return notification;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save notification for patient " + notification.getPatientId(), e);
        }
    }

    @Override
    public List<Notification> findByAppointmentNumber(int appointmentNumber) {
        String sql = "SELECT notification_id, appointment_number, patient_id, channel, recipient, message, sent_at "
                + "FROM notifications WHERE appointment_number = ? ORDER BY sent_at";
        List<Notification> notifications = new ArrayList<>();
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, appointmentNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Notification n = new Notification();
                    n.setNotificationId(rs.getInt("notification_id"));
                    n.setAppointmentNumber((Integer) rs.getObject("appointment_number"));
                    n.setPatientId(rs.getInt("patient_id"));
                    n.setChannel(rs.getString("channel"));
                    n.setRecipient(rs.getString("recipient"));
                    n.setMessage(rs.getString("message"));
                    n.setSentAt(rs.getObject("sent_at", java.time.LocalDateTime.class));
                    notifications.add(n);
                }
            }
            return notifications;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find notifications for appointment: " + appointmentNumber, e);
        }
    }
}
