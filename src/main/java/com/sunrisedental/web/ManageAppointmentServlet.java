package com.sunrisedental.web;

import com.sunrisedental.dao.NotificationDAO;
import com.sunrisedental.dao.impl.NotificationDAOImpl;
import com.sunrisedental.model.Appointment;
import com.sunrisedental.model.ChangeReason;
import com.sunrisedental.model.DelayDecision;
import com.sunrisedental.model.Dentist;
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
    private final NotificationService notificationService = new NotificationService();
    private final NotificationDAO notificationDAO = new NotificationDAOImpl();

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

        // Ownership check BEFORE acting, not just before displaying — a
        // Dentist could otherwise POST an action directly against another
        // dentist's appointment number without ever having seen it
        // rendered (showAppointment()'s own check happens too late to
        // stop that). Reported as "not found" rather than "not
        // authorized" — doesn't confirm to a Dentist that a given
        // appointment number belongs to someone else.
        if (findAuthorized(req, number).isEmpty()) {
            req.setAttribute("notFoundNumber", number);
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
        } catch (IllegalArgumentException e) {
            // Covers this class's own validation (minutes must be positive,
            // reschedule date not in the past — clear, specific messages),
            // ChangeReason.valueOf()/DelayDecision.valueOf() rejecting a
            // garbage enum value (a pre-existing gap: these used to reach
            // the servlet as an uncaught 500 rather than a normal form
            // error, since nothing here caught IllegalArgumentException
            // before this fix), and NumberFormatException (a subclass of
            // IllegalArgumentException — Integer.parseInt("abc")). The
            // enum/number cases' messages are Java's raw text rather than
            // something hand-written — reachable only by hand-crafting a
            // request or bypassing the number input's own browser
            // validation, so not worth a friendlier message for tonight.
            error = e.getMessage() != null ? e.getMessage() : "Please fill in all required fields correctly.";
        } catch (DateTimeParseException | NullPointerException e) {
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
                // Not on the use case diagram for Dentist (decision 8: a
                // dentist's involvement stays scoped to their own
                // clinical schedule — delay/reschedule — cancellation has
                // billing consequences and stays front-desk). The JSP
                // already disables this button for a Dentist; this is the
                // server-side enforcement a disabled button alone can't
                // provide against a hand-crafted request.
                if (RoleGuard.isDentist(req)) {
                    throw new IllegalStateException("Dentists cannot cancel appointments — please ask reception.");
                }
                ChangeReason reason = ChangeReason.valueOf(req.getParameter("reason"));
                Appointment cancelled = appointmentService.cancelAppointment(number, reason);
                notificationService.notifyAppointmentCancelled(cancelled);
                yield "Appointment cancelled.";
            }
            case "delay" -> handleDelay(req, number);
            case "reschedule" -> {
                LocalDate newDate = LocalDate.parse(req.getParameter("newDate"));
                LocalTime newTime = LocalTime.parse(req.getParameter("newTime"));
                if (newDate.isBefore(LocalDate.now())) {
                    throw new IllegalArgumentException("Reschedule date cannot be in the past.");
                }
                Appointment rescheduled = appointmentService.rescheduleAppointment(number, newDate, newTime);
                notificationService.notifyAppointmentRescheduled(rescheduled);
                yield "Appointment rescheduled to " + newDate + " at " + newTime + ".";
            }
            default -> throw new IllegalStateException("Unknown action: " + action);
        };
    }

    private String handleDelay(HttpServletRequest req, int number) {
        int minutes = Integer.parseInt(req.getParameter("minutes"));
        if (minutes <= 0) {
            throw new IllegalArgumentException("Delay minutes must be greater than zero.");
        }
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
            Optional<Appointment> appointment = findAuthorized(req, number);
            if (appointment.isPresent()) {
                req.setAttribute("appointment", appointment.get());
                req.setAttribute("notifications", notificationDAO.findByAppointmentNumber(number));
            } else {
                req.setAttribute("notFoundNumber", number);
            }
        }
        req.getRequestDispatcher("/WEB-INF/jsp/manage-appointment.jsp").forward(req, resp);
    }

    /**
     * A Receptionist/Administrator may look up any appointment (matches
     * Search Appointment's Receptionist/Administrator-only association on
     * the use case diagram — {@link SearchAppointmentServlet} enforces
     * the same thing for standalone browsing); a Dentist may only see
     * (and therefore only act on) appointments assigned to themself —
     * decision 6/14's Search Appointment {@code <<include>>} only ever
     * reaches Dentist through Record Appointment Delay's own use case,
     * not as a general lookup. Returns empty (not found) for a Dentist
     * looking up someone else's appointment, same as a genuinely missing
     * number — doesn't confirm a given number belongs to someone else.
     */
    private Optional<Appointment> findAuthorized(HttpServletRequest req, int number) {
        Optional<Appointment> appointment = appointmentService.searchAppointment(number);
        if (appointment.isEmpty() || !RoleGuard.isDentist(req)) {
            return appointment;
        }
        Dentist sessionDentist = (Dentist) req.getSession().getAttribute("loggedInUser");
        return appointment.get().getDentist().getDentistId() == sessionDentist.getDentistId()
                ? appointment : Optional.empty();
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
