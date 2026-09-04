package com.sunrisedental.service;

import com.sunrisedental.dao.DentistLoad;
import com.sunrisedental.dao.ReportDAO;
import com.sunrisedental.model.AppointmentStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Unit tests for the View Reports business tier (decision 5), against a stub ReportDAO. */
class ReportServiceTest {

    private static class StubReportDAO implements ReportDAO {
        @Override
        public Map<AppointmentStatus, Integer> countAppointmentsByStatus() {
            return Map.of(AppointmentStatus.SCHEDULED, 5, AppointmentStatus.CANCELLED, 2);
        }

        @Override
        public double totalRevenue() {
            return 12345.50;
        }

        @Override
        public List<DentistLoad> appointmentLoadByDentist(LocalDate date) {
            return List.of(new DentistLoad("Dr. Perera", 3, 9000.0));
        }
    }

    @Test
    void appointmentCountsByStatus_delegatesToReportDAO() {
        ReportService service = new ReportService(new StubReportDAO());

        assertEquals(5, service.appointmentCountsByStatus().get(AppointmentStatus.SCHEDULED));
    }

    @Test
    void totalRevenue_delegatesToReportDAO() {
        ReportService service = new ReportService(new StubReportDAO());

        assertEquals(12345.50, service.totalRevenue());
    }

    @Test
    void appointmentLoadByDentist_delegatesToReportDAO() {
        ReportService service = new ReportService(new StubReportDAO());

        List<DentistLoad> load = service.appointmentLoadByDentist(LocalDate.now());

        assertEquals(1, load.size());
        assertEquals("Dr. Perera", load.get(0).dentistName());
    }
}
