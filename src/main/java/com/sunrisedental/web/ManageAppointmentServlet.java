package com.sunrisedental.web;

import com.sunrisedental.model.Appointment;
import com.sunrisedental.model.ChangeReason;
import com.sunrisedental.model.DelayDecision;
import com.sunrisedental.service.AppointmentService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Presentation tier for Cancel Appointment, Record Appointment Delay, and
 * Reschedule Appointment (decisions 7, 10-12; sequence diagram 3.3) — one
 * servlet for all three actions on a given appointment, since they share
 * the same "look up an appointment, act on it, show the updated record"
 * shape, and staff naturally move between them (e.g. a SKIP decision
 * leads straight into rescheduling).
 *
 * <p>{@code GET ?number=N} shows the appointment plus the three action
 * forms; {@code POST} handles whichever form was submitted (an
 * {@code action} field says which) and re-shows the same page with a
 * result message. This forwards rather than redirects after POST — a
 * page refresh could in principle re-submit the last action, which is
 * an accepted simplification for an internal staff tool rather than an
 * oversight (see AppointmentServlet's registration flow for where a
 * proper POST-redirect-GET was worth the extra request instead).</p>
 */
@WebServlet(name = "ManageAppointmentServlet", urlPatterns = {"/appointments/manage"})
public class ManageAppointmentServlet extends HttpServlet {

    private final AppointmentService appointmentService = new AppointmentService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        showAppointment(req, resp, parseNumber(req.getParameter("number")));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer number = parseNumber(req.getParameter("number"));
        if (number == null) {
            req.setAttribute("error", "Appointment number is required.");
            req.getRequestDispatcher("/WEB-INF/jsp/manage-appointment.jsp").forward(req, resp);
            return;
        }

        String message = null;
        String error = null;
        try {
            message = performAction(req, number);
        } catch (IllegalStateException e) {
            error = e.getMessage(); // e.g. reschedule target slot unavailable
        } catch (NoSuchElementException e) {
            error = "Appointment " + number + " not found.";
        } catch (NumberFormatException | DateTimeParseException | NullPointerException e) {
            error = "Please fill in all required fields correctly.";
        }

        if (message != null) {
            req.setAttribute("message", message);
        }
        if (error != null) {
            req.setAttribute("error", error);
        }
        showAppointment(req, resp, number);
    }

    /** @return a human-readable success message for the JSP to display */
    private String performAction(HttpServletRequest req, int number) {
        String action = req.getParameter("action");
        return switch (action == null ? "" : action) {
            case "cancel" -> {
                ChangeReason reason = ChangeReason.valueOf(req.getParameter("reason"));
                appointmentService.cancelAppointment(number, reason);
                yield "Appointment cancelled.";
            }
            case "delay" -> handleDelay(req, number);
            case "reschedule" -> {
                LocalDate newDate = LocalDate.parse(req.getParameter("newDate"));
                LocalTime newTime = LocalTime.parse(req.getParameter("newTime"));
                appointmentService.rescheduleAppointment(number, newDate, newTime);
                yield "Appointment rescheduled to " + newDate + " at " + newTime + ".";
            }
            default -> throw new IllegalStateException("Unknown action: " + action);
        };
    }

    private String handleDelay(HttpServletRequest req, int number) {
        int minutes = Integer.parseInt(req.getParameter("minutes"));
        boolean dentistCaused = "dentist".equals(req.getParameter("who"));

        if (dentistCaused) {
            List<Appointment> affected = appointmentService.recordDentistDelay(number, minutes);
            return "Dentist delay recorded — " + affected.size() + " appointment(s) updated.";
        }

        DelayDecision decision = DelayDecision.valueOf(req.getParameter("decision"));
        appointmentService.recordPatientDelay(number, minutes, decision);
        return decision == DelayDecision.SKIP
                ? "Patient skipped — appointment cancelled. Reschedule below once a new slot is agreed."
                : "Patient delay recorded (waiting) — the rest of the dentist's day has shifted too.";
    }

    private void showAppointment(HttpServletRequest req, HttpServletResponse resp, Integer number)
            throws ServletException, IOException {
        if (number != null) {
            Optional<Appointment> appointment = appointmentService.searchAppointment(number);
            if (appointment.isPresent()) {
                req.setAttribute("appointment", appointment.get());
            } else {
                req.setAttribute("notFoundNumber", number);
            }
        }
        req.getRequestDispatcher("/WEB-INF/jsp/manage-appointment.jsp").forward(req, resp);
    }

    private Integer parseNumber(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
