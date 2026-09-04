package com.sunrisedental.web;

import com.sunrisedental.model.Appointment;
import com.sunrisedental.service.AppointmentService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Optional;

/**
 * Presentation tier for Display Appointment Details (search by
 * appointment number, brief requirement 3) — {@code GET /appointments}
 * with no {@code number} param shows a search box; with one, shows the
 * matching record or a not-found message. Also where
 * {@link AppointmentServlet}'s successful POST redirects to, showing the
 * just-booked appointment (POST-redirect-GET).
 */
@WebServlet(name = "SearchAppointmentServlet", urlPatterns = {"/appointments"})
public class SearchAppointmentServlet extends HttpServlet {

    private final AppointmentService appointmentService = new AppointmentService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String numberParam = req.getParameter("number");

        if (numberParam != null && !numberParam.isBlank()) {
            try {
                int appointmentNumber = Integer.parseInt(numberParam.trim());
                Optional<Appointment> appointment = appointmentService.searchAppointment(appointmentNumber);
                if (appointment.isPresent()) {
                    req.setAttribute("appointment", appointment.get());
                } else {
                    req.setAttribute("notFoundNumber", appointmentNumber);
                }
            } catch (NumberFormatException e) {
                req.setAttribute("error", "Appointment number must be a whole number.");
            }
        }

        req.getRequestDispatcher("/WEB-INF/jsp/appointment-search.jsp").forward(req, resp);
    }
}
