package com.sunrisedental.service;

import com.sunrisedental.dao.DentistDAO;
import com.sunrisedental.model.AvailabilityBlock;
import com.sunrisedental.model.Dentist;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Unit tests for the Set Daily Appointment Limit / Set Availability delegations (decision 13). */
class DentistServiceTest {

    private static class StubDentistDAO implements DentistDAO {
        Integer lastLimitDentistId;
        Integer lastLimit;
        Integer lastBlockDentistId;
        AvailabilityBlock lastBlock;

        @Override
        public Optional<Dentist> findById(int dentistId) {
            return Optional.empty();
        }

        @Override
        public List<Dentist> findAll() {
            return new ArrayList<>();
        }

        @Override
        public void updateDailyAppointmentLimit(int dentistId, int limit) {
            lastLimitDentistId = dentistId;
            lastLimit = limit;
        }

        @Override
        public void addAvailabilityBlock(int dentistId, AvailabilityBlock block) {
            lastBlockDentistId = dentistId;
            lastBlock = block;
        }

        @Override
        public List<AvailabilityBlock> findAvailabilityBlocks(int dentistId) {
            return List.of();
        }

        @Override
        public int countAppointmentsOnDate(int dentistId, LocalDate date, int excludeAppointmentNumber) {
            return 0;
        }
    }

    @Test
    void setDailyAppointmentLimit_delegatesToDentistDAO() {
        StubDentistDAO dao = new StubDentistDAO();
        DentistService service = new DentistService(dao);

        service.setDailyAppointmentLimit(1, 25);

        assertEquals(1, dao.lastLimitDentistId);
        assertEquals(25, dao.lastLimit);
    }

    @Test
    void setAvailability_delegatesToDentistDAO() {
        StubDentistDAO dao = new StubDentistDAO();
        DentistService service = new DentistService(dao);
        AvailabilityBlock block = new AvailabilityBlock(0,
                LocalDateTime.of(2026, 9, 10, 12, 0), LocalDateTime.of(2026, 9, 10, 13, 0), "Lunch");

        service.setAvailability(1, block);

        assertEquals(1, dao.lastBlockDentistId);
        assertTrue(dao.lastBlock == block);
    }
}
