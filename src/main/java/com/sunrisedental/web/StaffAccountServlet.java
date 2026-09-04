package com.sunrisedental.web;

import com.sunrisedental.model.Administrator;
import com.sunrisedental.model.Dentist;
import com.sunrisedental.model.Receptionist;
import com.sunrisedental.model.User;
import com.sunrisedental.service.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Presentation tier for Manage Staff Accounts (Administrator-only use
 * case, decision 5). {@link AuthenticationFilter} already guards this
 * path for "is someone logged in" — the extra role check here is
 * "is that someone an Administrator", which is specific to this one
 * servlet rather than a general cross-cutting concern, so it's inline
 * rather than a second filter.
 */
@WebServlet(name = "StaffAccountServlet", urlPatterns = {"/staff"})
public class StaffAccountServlet extends HttpServlet {

    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireAdmin(req, resp)) {
            return;
        }
        showStaffList(req, resp, null);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!requireAdmin(req, resp)) {
            return;
        }

        String action = req.getParameter("action");
        String error = null;
        try {
            if ("add".equals(action)) {
                String username = req.getParameter("username");
                String password = req.getParameter("password");
                String fullName = req.getParameter("fullName");
                String role = req.getParameter("role");
                if (isBlank(username) || isBlank(password) || isBlank(fullName) || isBlank(role)) {
                    error = "Username, password, full name and role are all required.";
                } else {
                    User newUser = switch (role) {
                        case "ADMINISTRATOR" -> new Administrator(0, username, password, fullName);
                        case "DENTIST" -> new Dentist(0, username, password, fullName, 0, 20);
                        default -> new Receptionist(0, username, password, fullName);
                    };
                    userService.addStaffAccount(newUser);
                }
            } else if ("remove".equals(action)) {
                userService.removeStaffAccount(Integer.parseInt(req.getParameter("userId")));
            }
        } catch (NumberFormatException e) {
            error = "Invalid user id.";
        } catch (RuntimeException e) {
            error = "That username is already taken."; // most likely cause: users.username UNIQUE constraint
        }
        showStaffList(req, resp, error);
    }

    /** Plain view row so the JSP never needs to reflectively ask a domain object for its own class name. */
    public record StaffRow(int userId, String username, String fullName, String role) {
    }

    private void showStaffList(HttpServletRequest req, HttpServletResponse resp, String error)
            throws ServletException, IOException {
        List<StaffRow> rows = userService.listStaff().stream()
                .map(user -> new StaffRow(user.getUserId(), user.getUsername(), user.getFullName(),
                        user instanceof Administrator ? "ADMINISTRATOR"
                                : user instanceof Dentist ? "DENTIST"
                                : "RECEPTIONIST"))
                .toList();
        req.setAttribute("staff", rows);
        if (error != null) {
            req.setAttribute("error", error);
        }
        req.getRequestDispatcher("/WEB-INF/jsp/staff-accounts.jsp").forward(req, resp);
    }

    private boolean requireAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Object isAdmin = req.getSession().getAttribute("isAdmin");
        if (!Boolean.TRUE.equals(isAdmin)) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return false;
        }
        return true;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
