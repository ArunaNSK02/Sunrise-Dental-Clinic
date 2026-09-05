package com.sunrisedental.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Small, shared role checks for servlets that need more than just "is
 * someone logged in" ({@link AuthenticationFilter} already covers that).
 * Pulled out once four different servlets each had their own near-
 * identical private {@code requireXxx} method (StaffAccountServlet,
 * ReportsServlet, DentistSettingsServlet, and this refactor's own
 * trigger — a real role/functionality mismatch a user caught by testing
 * as a Dentist and seeing Register/Search/Bill in the nav, which none of
 * those three servlets had ever actually blocked at the server side).
 *
 * <p>Every method redirects to {@code /dashboard} and returns
 * {@code false} on denial, {@code true} on success — callers should
 * {@code return} immediately when a check returns {@code false}.</p>
 */
final class RoleGuard {

    private RoleGuard() {
    }

    /** Administrator only — Manage Staff Accounts, View Reports (decision 5). */
    static boolean requireAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        return require(req, resp, isAdmin(req));
    }

    /** A Dentist managing their own record, or an Administrator overriding any — Set Daily Appointment Limit/Availability (decision 13). */
    static boolean requireDentistOrAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        return require(req, resp, isDentist(req) || isAdmin(req));
    }

    /**
     * Receptionist or Administrator only, never a Dentist — Register New
     * Appointment, Display Appointment Details, Calculate &amp; Print
     * Bill. The use case diagram never associates Dentist with any of
     * these three (decision 8: a dentist's involvement stays scoped to
     * their own clinical schedule — delay/reschedule/availability — not
     * front-desk registration, billing, or browsing arbitrary records).
     */
    static boolean requireNotDentist(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        return require(req, resp, !isDentist(req));
    }

    private static boolean require(HttpServletRequest req, HttpServletResponse resp, boolean allowed)
            throws IOException {
        if (!allowed) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return false;
        }
        return true;
    }

    /** Package-private (not just used internally here) — for a plain yes/no check with no redirect side effect, e.g. ManageAppointmentServlet scoping a Dentist to their own appointments. */
    static boolean isAdmin(HttpServletRequest req) {
        HttpSession session = req.getSession();
        return Boolean.TRUE.equals(session.getAttribute("isAdmin"));
    }

    static boolean isDentist(HttpServletRequest req) {
        HttpSession session = req.getSession();
        return Boolean.TRUE.equals(session.getAttribute("isDentist"));
    }
}
