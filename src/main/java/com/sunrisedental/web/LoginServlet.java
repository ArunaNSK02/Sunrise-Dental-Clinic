package com.sunrisedental.web;

import com.sunrisedental.model.Administrator;
import com.sunrisedental.model.Dentist;
import com.sunrisedental.model.User;
import com.sunrisedental.service.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Optional;

/**
 * Presentation tier — first Servlet in the project, matching sequence
 * diagram 3.1 (Login) and completing the Servlet -&gt; Service -&gt; DAO -&gt;
 * Database chain: {@link UserService} does the credential check, this
 * class only decides what an HTTP request/response does with the result
 * (redirect + session on success, error message on failure) — exactly
 * where the diagram's {@code alt} fragment says session handling belongs.
 *
 * <p>Annotation-based routing (Jakarta Servlet 6.0 / Tomcat 11), per
 * CLAUDE.md — no web.xml servlet-mapping entry needed.</p>
 */
@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {

    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        Optional<User> authenticated = userService.login(username, password);

        if (authenticated.isPresent()) {
            User user = authenticated.get();
            HttpSession session = req.getSession(true);
            // Session fixation defense: if a session already existed before
            // login (e.g. an attacker got a victim to visit a link carrying
            // a known session id), swap in a fresh id now that the user is
            // authenticated, rather than letting a pre-login id become a
            // valid authenticated one. changeSessionId() keeps the same
            // session object/attributes, just gives it a new id.
            req.changeSessionId();
            session.setAttribute("loggedInUser", user);
            // Plain booleans rather than relying on JSP EL's getClass()
            // reflection trick to branch menu visibility per role —
            // simpler and more obviously correct in the JSPs.
            session.setAttribute("isAdmin", user instanceof Administrator);
            session.setAttribute("isDentist", user instanceof Dentist);
            resp.sendRedirect(req.getContextPath() + "/dashboard");
        } else {
            req.setAttribute("error", "Invalid username or password.");
            req.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(req, resp);
        }
    }
}
