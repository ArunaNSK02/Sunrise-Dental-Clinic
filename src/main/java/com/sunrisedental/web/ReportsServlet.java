package com.sunrisedental.web;

import com.sunrisedental.service.ReportService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Presentation tier for View Reports (Administrator-only use case,
 * decision 5) — appointment counts by status, total revenue, and
 * per-dentist load for a chosen date (defaulting to today).
 */
@WebServlet(name = "ReportsServlet", urlPatterns = {"/reports"})
public class ReportsServlet extends HttpServlet {

    private final ReportService reportService = new ReportService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!Boolean.TRUE.equals(req.getSession().getAttribute("isAdmin"))) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }

        LocalDate date;
        try {
            String dateParam = req.getParameter("date");
            date = (dateParam == null || dateParam.isBlank()) ? LocalDate.now() : LocalDate.parse(dateParam);
        } catch (DateTimeParseException e) {
            date = LocalDate.now();
        }

        req.setAttribute("statusCounts", reportService.appointmentCountsByStatus());
        req.setAttribute("totalRevenue", reportService.totalRevenue());
        req.setAttribute("dentistLoad", reportService.appointmentLoadByDentist(date));
        req.setAttribute("reportDate", date);
        req.getRequestDispatcher("/WEB-INF/jsp/reports.jsp").forward(req, resp);
    }
}
