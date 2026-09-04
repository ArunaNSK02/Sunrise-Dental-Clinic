package com.sunrisedental.service;

import com.sunrisedental.dao.BillDAO;
import com.sunrisedental.dao.ClinicSettingsDAO;
import com.sunrisedental.dao.impl.BillDAOImpl;
import com.sunrisedental.dao.impl.ClinicSettingsDAOImpl;
import com.sunrisedental.model.Appointment;
import com.sunrisedental.model.Bill;

import java.time.LocalDate;

/**
 * Business tier for Calculate &amp; Print Bill (brief requirement 4).
 * {@code Appointment.generateBill()} stays a model-layer placeholder
 * (decision 17 keeps state-changing behaviour on Appointment itself, but
 * the working implementation needs DAO access the model doesn't have —
 * the same reason {@code Receptionist.registerAppointment()} is a
 * placeholder delegating to {@link AppointmentService}) — this class is
 * where the real logic lives.
 */
public class BillService {

    private final BillDAO billDAO;
    private final ClinicSettingsDAO clinicSettingsDAO;

    public BillService() {
        this(new BillDAOImpl(), new ClinicSettingsDAOImpl());
    }

    public BillService(BillDAO billDAO, ClinicSettingsDAO clinicSettingsDAO) {
        this.billDAO = billDAO;
        this.clinicSettingsDAO = clinicSettingsDAO;
    }

    /**
     * Returns the appointment's bill, generating and persisting one on
     * first request if it doesn't exist yet — idempotent, so viewing a
     * bill twice never creates a duplicate row (the DB's UNIQUE constraint
     * on {@code appointment_number} backs this up regardless).
     */
    public Bill getOrGenerateBill(Appointment appointment) {
        return billDAO.findByAppointmentNumber(appointment.getAppointmentNumber())
                .orElseGet(() -> {
                    double treatmentCost = appointment.getTreatment().getCost();
                    double consultationFee = clinicSettingsDAO.getConsultationFee();
                    Bill bill = new Bill(0, treatmentCost, consultationFee, LocalDate.now());
                    return billDAO.save(appointment.getAppointmentNumber(), bill);
                });
    }
}
