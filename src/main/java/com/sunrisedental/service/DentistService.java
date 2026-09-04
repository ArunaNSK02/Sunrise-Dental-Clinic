package com.sunrisedental.service;

import com.sunrisedental.dao.DentistDAO;
import com.sunrisedental.dao.impl.DentistDAOImpl;
import com.sunrisedental.model.AvailabilityBlock;
import com.sunrisedental.model.Dentist;

import java.util.List;
import java.util.Optional;

/**
 * Business tier for Set Daily Appointment Limit and Set Availability
 * (decision 13) — reachable by a Dentist for their own record, or an
 * Administrator as an override for any dentist. Thin delegation to
 * {@link DentistDAO}; there's no extra business rule beyond "persist
 * what was asked" here, unlike {@code AppointmentService}'s availability
 * checks.
 */
public class DentistService {

    private final DentistDAO dentistDAO;

    public DentistService() {
        this(new DentistDAOImpl());
    }

    public DentistService(DentistDAO dentistDAO) {
        this.dentistDAO = dentistDAO;
    }

    public Optional<Dentist> findById(int dentistId) {
        return dentistDAO.findById(dentistId);
    }

    public List<Dentist> findAll() {
        return dentistDAO.findAll();
    }

    public void setDailyAppointmentLimit(int dentistId, int limit) {
        dentistDAO.updateDailyAppointmentLimit(dentistId, limit);
    }

    public void setAvailability(int dentistId, AvailabilityBlock block) {
        dentistDAO.addAvailabilityBlock(dentistId, block);
    }
}
