package com.sunrisedental.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Help Section (brief requirement 5) — static in-app, step-by-step
 * instructions for new staff. No business logic, so no service tier;
 * content lives entirely in help.jsp.
 */
@WebServlet(name = "HelpServlet", urlPatterns = {"/help"})
public class HelpServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/jsp/help.jsp").forward(req, resp);
    }
}
