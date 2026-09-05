package com.sunrisedental.web;

import com.sunrisedental.model.AvailabilityBlock;
import com.sunrisedental.model.Dentist;
import com.sunrisedental.service.DentistService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Presentation tier for Set Daily Appointment Limit / Set Availability
 * (decision 13). A logged-in Dentist manages their own record (their
 * {@code dentistId} comes from the session, never a request parameter —
 * a Dentist can't be trusted to pass someone else's id); an
 * Administrator picks which dentist to override via a {@code dentistId}
 * query/form parameter, since they're allowed to act on any of them.
 */
@WebServlet(name = "DentistSettingsServlet", urlPatterns = {"/dentist/settings"})
public class DentistSettingsServlet extends HttpServlet {

    private final DentistService dentistService = new DentistService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!RoleGuard.requireDentistOrAdmin(req, resp)) {
            return;
        }
        Integer dentistId = resolveDentistId(req);
        if (dentistId == null) {
            req.setAttribute("dentists", dentistService.findAll()); // Admin: show the picker
            req.getRequestDispatcher("/WEB-INF/jsp/dentist-settings.jsp").forward(req, resp);
            return;
        }
        showSettings(req, resp, dentistId, null);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!RoleGuard.requireDentistOrAdmin(req, resp)) {
            return;
        }
        Integer dentistId = resolveDentistId(req);
        if (dentistId == null) {
            resp.sendRedirect(req.getContextPath() + "/dentist/settings");
            return;
        }

        String error = null;
        try {
            String action = req.getParameter("action");
            if ("limit".equals(action)) {
                int limit = Integer.parseInt(req.getParameter("limit"));
                if (limit <= 0) {
                    throw new IllegalArgumentException("Daily appointment limit must be greater than zero.");
                }
                dentistService.setDailyAppointmentLimit(dentistId, limit);
            } else if ("availability".equals(action)) {
                LocalDateTime start = LocalDateTime.parse(req.getParameter("start"));
                LocalDateTime end = LocalDateTime.parse(req.getParameter("end"));
                if (!end.isAfter(start)) {
                    throw new IllegalArgumentException("The end of an unavailable period must be after its start.");
                }
                String reason = req.getParameter("reason");
                dentistService.setAvailability(dentistId, new AvailabilityBlock(0, start, end, reason));
            }
        } catch (IllegalArgumentException e) {
            error = e.getMessage() != null ? e.getMessage() : "Please fill in all fields correctly.";
        } catch (RuntimeException e) {
            error = "Please fill in all fields correctly.";
        }
        showSettings(req, resp, dentistId, error);
    }

    private void showSettings(HttpServletRequest req, HttpServletResponse resp, int dentistId, String error)
            throws ServletException, IOException {
        Optional<Dentist> dentist = dentistService.findById(dentistId);
        if (dentist.isEmpty()) {
            req.setAttribute("error", "Dentist not found.");
        } else {
            req.setAttribute("dentist", dentist.get());
            if (error != null) {
                req.setAttribute("error", error);
            }
        }
        req.getRequestDispatcher("/WEB-INF/jsp/dentist-settings.jsp").forward(req, resp);
    }

    /** @return the dentist to manage, or null if an Administrator hasn't picked one yet */
    private Integer resolveDentistId(HttpServletRequest req) {
        HttpSession session = req.getSession();
        if (Boolean.TRUE.equals(session.getAttribute("isDentist"))) {
            return ((Dentist) session.getAttribute("loggedInUser")).getDentistId();
        }
        String param = req.getParameter("dentistId");
        if (param == null || param.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(param.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
