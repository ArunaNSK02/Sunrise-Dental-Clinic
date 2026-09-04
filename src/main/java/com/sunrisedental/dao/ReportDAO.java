package com.sunrisedental.dao;

import com.sunrisedental.model.AppointmentStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Decision-useful reporting queries for Administrator's View Reports use
 * case (decision 5) — aggregate SQL that doesn't belong to any single
 * entity's own DAO, so it gets its own, matching the DAO pattern used
 * everywhere else rather than putting raw SQL in the service/servlet.
 */
public interface ReportDAO {

    /** How many appointments are currently in each status, clinic-wide. */
    Map<AppointmentStatus, Integer> countAppointmentsByStatus();

    /** Total revenue billed to date (sum of every bill's total_amount). */
    double totalRevenue();

    /** How many appointments each dentist has on the given date (excluding cancelled). */
    List<DentistLoad> appointmentLoadByDentist(LocalDate date);
}
