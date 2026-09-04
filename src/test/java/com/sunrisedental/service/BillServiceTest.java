package com.sunrisedental.service;

import com.sunrisedental.dao.BillDAO;
import com.sunrisedental.dao.ClinicSettingsDAO;
import com.sunrisedental.model.Appointment;
import com.sunrisedental.model.Bill;
import com.sunrisedental.model.Dentist;
import com.sunrisedental.model.Patient;
import com.sunrisedental.model.Treatment;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class BillServiceTest {

    private final Appointment appointment = new Appointment(
            1, new Patient(1, "Jane Silva", "1 Galle Rd", "0771234567"),
            new Dentist(1, "d.perera", "x", "Dr. Perera", 1, 20),
            new Treatment(1, "Filling", 4000.0, 30),
            LocalDate.of(2026, 9, 10), LocalTime.of(10, 0));

    @Test
    void getOrGenerateBill_generatesAndSavesANewBillUsingTheClinicWideConsultationFee() {
        StubBillDAO billDAO = new StubBillDAO();
        BillService service = new BillService(billDAO, () -> 1500.0);

        Bill bill = service.getOrGenerateBill(appointment);

        assertEquals(4000.0, bill.getTreatmentCost());
        assertEquals(1500.0, bill.getConsultationFee());
        assertEquals(5500.0, bill.getTotalAmount());
        assertEquals(1, billDAO.savedBills.size());
    }

    @Test
    void getOrGenerateBill_returnsTheExistingBillWithoutGeneratingAnother() {
        StubBillDAO billDAO = new StubBillDAO();
        Bill existing = new Bill(9, 4000.0, 1000.0, LocalDate.of(2026, 9, 1));
        billDAO.byAppointmentNumber.put(1, existing);
        BillService service = new BillService(billDAO, () -> 1500.0);

        Bill bill = service.getOrGenerateBill(appointment);

        assertSame(existing, bill);
        assertEquals(0, billDAO.savedBills.size());
    }

    private static class StubBillDAO implements BillDAO {
        final Map<Integer, Bill> byAppointmentNumber = new HashMap<>();
        final java.util.List<Bill> savedBills = new java.util.ArrayList<>();

        @Override
        public Optional<Bill> findByAppointmentNumber(int appointmentNumber) {
            return Optional.ofNullable(byAppointmentNumber.get(appointmentNumber));
        }

        @Override
        public Bill save(int appointmentNumber, Bill bill) {
            bill.setBillId(savedBills.size() + 1);
            savedBills.add(bill);
            byAppointmentNumber.put(appointmentNumber, bill);
            return bill;
        }
    }
}
