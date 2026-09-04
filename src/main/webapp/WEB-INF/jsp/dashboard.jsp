<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Dashboard — Sunrise Dental Clinic</title>
</head>
<body>
    <h1>Sunrise Dental Clinic</h1>
    <p>Welcome, ${sessionScope.loggedInUser.fullName}.
       <a href="${pageContext.request.contextPath}/logout">Log out</a></p>

    <ul>
        <li><a href="${pageContext.request.contextPath}/appointments/new">Register New Appointment</a></li>
        <li><a href="${pageContext.request.contextPath}/appointments">Display Appointment Details (search)</a></li>
        <li><a href="${pageContext.request.contextPath}/appointments/bill">Calculate &amp; Print Bill</a></li>
        <li>Help — coming soon</li>
    </ul>
</body>
</html>
