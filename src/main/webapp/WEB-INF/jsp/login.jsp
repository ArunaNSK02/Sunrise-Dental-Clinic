<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Staff Login — Sunrise Dental Clinic</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <div class="auth-page">
        <div class="auth-card">
            <h1>Sunrise Dental Clinic</h1>
            <p class="subtitle">Staff login</p>

            <% if (request.getAttribute("error") != null) { %>
                <div class="alert alert-error"><%= request.getAttribute("error") %></div>
            <% } %>

            <form method="post" action="${pageContext.request.contextPath}/login">
                <label>Username
                    <input type="text" name="username" required autofocus>
                </label>
                <label>Password
                    <input type="password" name="password" required>
                </label>
                <button type="submit">Log in</button>
            </form>
        </div>
    </div>
</body>
</html>
