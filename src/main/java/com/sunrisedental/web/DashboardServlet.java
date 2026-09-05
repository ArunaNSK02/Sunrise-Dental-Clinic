package com.sunrisedental.web;

import com.sunrisedental.dao.AppointmentDAO;
import com.sunrisedental.dao.impl.AppointmentDAOImpl;
import com.sunrisedental.model.Appointment;
import com.sunrisedental.model.Dentist;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/**
 * The staff menu (brief's "menu-driven" requirement) — landing page after
 * Login. Protected by {@link AuthenticationFilter}, not a session check
 * here.
 *
 * <p>Shows a real "Today's Schedule" instead of repeating the top nav's
 * links in the page body (that duplication was flagged directly — the
 * nav already covers navigation, so the body's job is to show something
 * the nav can't: today's actual clinic activity). A Dentist sees only
 * their own day; a Receptionist/Administrator sees the whole clinic's,
 * since they coordinate across dentists.</p>
 */
@WebServlet(name = "DashboardServlet", urlPatterns = {"/dashboard"})
public class DashboardServlet extends HttpServlet {

    private final AppointmentDAO appointmentDAO = new AppointmentDAOImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        LocalDate today = LocalDate.now();

        List<Appointment> todaysAppointments;
        if (Boolean.TRUE.equals(session.getAttribute("isDentist"))) {
            Dentist dentist = (Dentist) session.getAttribute("loggedInUser");
            todaysAppointments = appointmentDAO.findByDentistAndDate(dentist.getDentistId(), today);
        } else {
            todaysAppointments = appointmentDAO.findByDate(today);
        }

        req.setAttribute("todaysAppointments", todaysAppointments);
        req.setAttribute("todayDate", today);
        req.getRequestDispatcher("/WEB-INF/jsp/dashboard.jsp").forward(req, resp);
    }
}
