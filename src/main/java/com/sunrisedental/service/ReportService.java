package com.sunrisedental.service;

import com.sunrisedental.dao.DentistLoad;
import com.sunrisedental.dao.ReportDAO;
import com.sunrisedental.dao.impl.ReportDAOImpl;
import com.sunrisedental.model.AppointmentStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Business tier for Administrator's View Reports use case (decision 5). */
public class ReportService {

    private final ReportDAO reportDAO;

    public ReportService() {
        this(new ReportDAOImpl());
    }

    public ReportService(ReportDAO reportDAO) {
        this.reportDAO = reportDAO;
    }

    public Map<AppointmentStatus, Integer> appointmentCountsByStatus() {
        return reportDAO.countAppointmentsByStatus();
    }

    public double totalRevenue() {
        return reportDAO.totalRevenue();
    }

    public List<DentistLoad> appointmentLoadByDentist(LocalDate date) {
        return reportDAO.appointmentLoadByDentist(date);
    }
}
