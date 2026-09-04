package com.sunrisedental.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Session-based login guard (CLAUDE.md's session/cookie requirement),
 * kept as a Filter rather than duplicated per-servlet session-checking
 * code — a cross-cutting concern belongs in front of the servlets, not
 * copy-pasted into each one. Any request under a protected path without
 * a {@code loggedInUser} session attribute is redirected to {@code /login}
 * instead of reaching the servlet.
 */
@WebFilter(urlPatterns = {
        "/dashboard", "/appointments/*", "/help", "/staff", "/staff/*", "/reports", "/dentist/*"
})
public class AuthenticationFilter implements jakarta.servlet.Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        HttpSession session = req.getSession(false);
        boolean loggedIn = session != null && session.getAttribute("loggedInUser") != null;

        if (loggedIn) {
            chain.doFilter(request, response);
        } else {
            resp.sendRedirect(req.getContextPath() + "/login");
        }
    }
}
