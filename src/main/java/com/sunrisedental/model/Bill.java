package com.sunrisedental.model;

import java.time.LocalDate;

/**
 * A receipt for one appointment (class diagram, docs/DESIGN.md). Composed
 * by {@link Appointment} — a bill has no meaning outside the appointment
 * it belongs to and should be removed automatically when the appointment
 * is (decision 19).
 */
public class Bill {

    private int billId;
    private double treatmentCost;
    private double consultationFee;
    private double totalAmount;
    private LocalDate issueDate;

    public Bill() {
    }

    public Bill(int billId, double treatmentCost, double consultationFee, LocalDate issueDate) {
        this.billId = billId;
        this.treatmentCost = treatmentCost;
        this.consultationFee = consultationFee;
        this.issueDate = issueDate;
        this.totalAmount = calculateTotal();
    }

    /** Total = treatment cost + consultation fee, per the brief's billing requirement. */
    public double calculateTotal() {
        this.totalAmount = treatmentCost + consultationFee;
        return totalAmount;
    }

    /**
     * Placeholder matching the class diagram's method signature. The real
     * implementation formats and renders a printable receipt — likely a
     * Factory-pattern candidate for Task B (bill/report creation), per
     * CLAUDE.md's suggested pattern list.
     */
    public void print() {
        throw new UnsupportedOperationException("Receipt rendering not yet implemented.");
    }

    public int getBillId() {
        return billId;
    }

    public void setBillId(int billId) {
        this.billId = billId;
    }

    public double getTreatmentCost() {
        return treatmentCost;
    }

    public void setTreatmentCost(double treatmentCost) {
        this.treatmentCost = treatmentCost;
    }

    public double getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(double consultationFee) {
        this.consultationFee = consultationFee;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }
}
