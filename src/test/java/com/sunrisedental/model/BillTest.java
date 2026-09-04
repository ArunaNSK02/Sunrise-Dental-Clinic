package com.sunrisedental.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * First JUnit test in the project (Task C). Covers the brief's core
 * billing requirement directly: total = treatment cost + consultation
 * fee. Domain logic only — no database, deliberately, so this test class
 * runs without a MySQL connection and can be the first thing demonstrated
 * green in the report's test-automation screenshots.
 */
class BillTest {

    @Test
    void calculateTotal_addsTreatmentCostAndConsultationFee() {
        Bill bill = new Bill(1, 5000.00, 1500.00, LocalDate.of(2026, 9, 4));

        double total = bill.calculateTotal();

        assertEquals(6500.00, total, 0.001);
    }

    @Test
    void constructor_calculatesTotalUpFront() {
        Bill bill = new Bill(2, 2000.00, 1000.00, LocalDate.of(2026, 9, 4));

        assertEquals(3000.00, bill.getTotalAmount(), 0.001);
    }
}
