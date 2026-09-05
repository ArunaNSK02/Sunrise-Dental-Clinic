package com.sunrisedental.web;

import com.sunrisedental.dao.DentistDAO;
import com.sunrisedental.dao.TreatmentDAO;
import com.sunrisedental.dao.impl.DentistDAOImpl;
import com.sunrisedental.dao.impl.TreatmentDAOImpl;
import com.sunrisedental.model.Appointment;
import com.sunrisedental.model.Dentist;
import com.sunrisedental.model.Patient;
import com.sunrisedental.model.Treatment;
import com.sunrisedental.service.AppointmentService;
import com.sunrisedental.service.NotificationService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Presentation tier for Register New Appointment, matching sequence
 * diagram 3.2: this servlet only handles HTTP concerns (reading form
 * fields, validation, choosing which JSP to render) and delegates the
 * find-or-register-patient step and the availability check + persist to
 * {@link AppointmentService}. On success it redirects to
 * {@link SearchAppointmentServlet} for the newly booked appointment
 * number — POST-redirect-GET, so refreshing the confirmation page never
 * re-submits the booking.
 */
@WebServlet(name = "AppointmentServlet", urlPatterns = {"/appointments/new"})
public class AppointmentServlet extends HttpServlet {

    // Sri Lankan-style contact numbers: 0 or +94 followed by 9 digits (e.g. 0771234567,
    // +94771234567) — a real format check rather than only "not blank", per the brief's
    // "with validation" requirement.
    private static final Pattern CONTACT_NUMBER = Pattern.compile("^(?:\\+94|0)\\d{9}$");

    private final AppointmentService appointmentService = new AppointmentService();
    private final DentistDAO dentistDAO = new DentistDAOImpl();
    private final TreatmentDAO treatmentDAO = new TreatmentDAOImpl();
    private final NotificationService notificationService = new NotificationService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!RoleGuard.requireNotDentist(req, resp)) {
            return;
        }
        showForm(req, resp, null);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!RoleGuard.requireNotDentist(req, resp)) {
            return;
        }
        String name = trim(req.getParameter("patientName"));
        String address = trim(req.getParameter("address"));
        String contactNumber = trim(req.getParameter("contactNumber"));

        if (name.isEmpty() || address.isEmpty() || contactNumber.isEmpty()) {
            showForm(req, resp, "Patient name, address and contact number are all required.");
            return;
        }
        if (!CONTACT_NUMBER.matcher(contactNumber).matches()) {
            showForm(req, resp, "Contact number must be a valid Sri Lankan number, e.g. 0771234567 or +94771234567.");
            return;
        }

        Dentist dentist;
        Treatment treatment;
        LocalDate date;
        LocalTime time;
        try {
            dentist = dentistDAO.findById(Integer.parseInt(req.getParameter("dentistId")))
                    .orElseThrow(() -> new IllegalArgumentException("Unknown dentist"));
            treatment = treatmentDAO.findById(Integer.parseInt(req.getParameter("treatmentId")))
                    .orElseThrow(() -> new IllegalArgumentException("Unknown treatment"));
            date = LocalDate.parse(req.getParameter("date"));
            time = LocalTime.parse(req.getParameter("time"));
        } catch (DateTimeParseException | IllegalArgumentException e) {
            showForm(req, resp, "Please choose a dentist, treatment, date and time.");
            return;
        }

        if (date.isBefore(LocalDate.now())) {
            showForm(req, resp, "Appointment date cannot be in the past.");
            return;
        }

        if (!appointmentService.checkDentistAvailability(dentist, date, time, treatment)) {
            showForm(req, resp, "Dr. " + dentist.getFullName() + " is not available on " + date
                    + " at " + time + " — please choose a different slot.");
            return;
        }

        Patient patient = appointmentService.findOrRegisterPatient(name, address, contactNumber);
        Appointment appointment = appointmentService.registerAppointment(patient, dentist, treatment, date, time);
        notificationService.notifyAppointmentBooked(appointment); // best-effort — never blocks the booking

        resp.sendRedirect(req.getContextPath() + "/appointments?number=" + appointment.getAppointmentNumber());
    }

    private void showForm(HttpServletRequest req, HttpServletResponse resp, String error)
            throws ServletException, IOException {
        req.setAttribute("dentists", dentistDAO.findAll());
        req.setAttribute("treatments", treatmentDAO.findAll());
        if (error != null) {
            req.setAttribute("error", error);
        }
        req.getRequestDispatcher("/WEB-INF/jsp/appointment-form.jsp").forward(req, resp);
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
