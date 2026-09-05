package com.sunrisedental.web;

import com.sunrisedental.model.Appointment;
import com.sunrisedental.model.Bill;
import com.sunrisedental.service.AppointmentService;
import com.sunrisedental.service.BillService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Optional;

/**
 * Presentation tier for Calculate &amp; Print Bill (brief requirement 4).
 * {@code GET /appointments/bill?number=N} — under {@code /appointments/*}
 * so {@link AuthenticationFilter} already guards it without a separate
 * mapping. Looks the appointment up, delegates to {@link BillService} to
 * get-or-generate its bill, and renders a printable receipt (the JSP has
 * a "Print" button using the browser's own print dialog rather than a
 * server-generated PDF — the brief asks for a printed receipt, not
 * specifically a file).
 */
@WebServlet(name = "BillServlet", urlPatterns = {"/appointments/bill"})
public class BillServlet extends HttpServlet {

    private final AppointmentService appointmentService = new AppointmentService();
    private final BillService billService = new BillService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!RoleGuard.requireNotDentist(req, resp)) {
            return;
        }
        String numberParam = req.getParameter("number");
        if (numberParam == null || numberParam.isBlank()) {
            req.getRequestDispatcher("/WEB-INF/jsp/bill.jsp").forward(req, resp);
            return;
        }

        int appointmentNumber;
        try {
            appointmentNumber = Integer.parseInt(numberParam.trim());
        } catch (NumberFormatException e) {
            req.setAttribute("error", "Appointment number must be a whole number.");
            req.getRequestDispatcher("/WEB-INF/jsp/bill.jsp").forward(req, resp);
            return;
        }

        Optional<Appointment> appointment = appointmentService.searchAppointment(appointmentNumber);
        if (appointment.isEmpty()) {
            req.setAttribute("error", "No appointment found with number " + appointmentNumber + ".");
            req.getRequestDispatcher("/WEB-INF/jsp/bill.jsp").forward(req, resp);
            return;
        }

        Bill bill = billService.getOrGenerateBill(appointment.get());
        req.setAttribute("appointment", appointment.get());
        req.setAttribute("bill", bill);
        req.getRequestDispatcher("/WEB-INF/jsp/bill.jsp").forward(req, resp);
    }
}
