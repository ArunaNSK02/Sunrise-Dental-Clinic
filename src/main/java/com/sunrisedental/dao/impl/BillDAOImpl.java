package com.sunrisedental.dao.impl;

import com.sunrisedental.dao.BillDAO;
import com.sunrisedental.db.DBConnectionManager;
import com.sunrisedental.model.Bill;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Optional;

public class BillDAOImpl implements BillDAO {

    @Override
    public Optional<Bill> findByAppointmentNumber(int appointmentNumber) {
        String sql = "SELECT bill_id, treatment_cost, consultation_fee, total_amount, issue_date "
                + "FROM bills WHERE appointment_number = ?";
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, appointmentNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find bill for appointment: " + appointmentNumber, e);
        }
    }

    @Override
    public Bill save(int appointmentNumber, Bill bill) {
        // total_amount is computed by fn_bill_total(cost, fee) in SQL
        // (schema.sql) rather than passing bill.getTotalAmount() from
        // Java — the brief's core billing rule (decision 26) enforced at
        // both tiers instead of trusted to stay in sync by coincidence.
        String sql = "INSERT INTO bills (appointment_number, treatment_cost, consultation_fee, "
                + "total_amount, issue_date) VALUES (?, ?, ?, fn_bill_total(?, ?), ?)";
        try (Connection conn = DBConnectionManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, appointmentNumber);
            stmt.setDouble(2, bill.getTreatmentCost());
            stmt.setDouble(3, bill.getConsultationFee());
            stmt.setDouble(4, bill.getTreatmentCost());
            stmt.setDouble(5, bill.getConsultationFee());
            stmt.setObject(6, bill.getIssueDate());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    bill.setBillId(keys.getInt(1));
                }
            }
            return bill;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save bill for appointment: " + appointmentNumber, e);
        }
    }

    private Bill mapRow(ResultSet rs) throws SQLException {
        Bill bill = new Bill();
        bill.setBillId(rs.getInt("bill_id"));
        bill.setTreatmentCost(rs.getDouble("treatment_cost"));
        bill.setConsultationFee(rs.getDouble("consultation_fee"));
        bill.calculateTotal();
        bill.setIssueDate(rs.getObject("issue_date", LocalDate.class));
        return bill;
    }
}
