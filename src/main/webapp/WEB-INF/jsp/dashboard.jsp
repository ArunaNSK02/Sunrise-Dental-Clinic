<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
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
        <li><a href="${pageContext.request.contextPath}/appointments/manage">Cancel / Delay / Reschedule Appointment</a></li>
        <li><a href="${pageContext.request.contextPath}/help">Help</a></li>

        <c:if test="${sessionScope.isDentist or sessionScope.isAdmin}">
            <li><a href="${pageContext.request.contextPath}/dentist/settings">Set Daily Appointment Limit / Availability</a></li>
        </c:if>
        <c:if test="${sessionScope.isAdmin}">
            <li><a href="${pageContext.request.contextPath}/staff">Manage Staff Accounts (Admin)</a></li>
            <li><a href="${pageContext.request.contextPath}/reports">View Reports (Admin)</a></li>
        </c:if>
    </ul>
</body>
</html>
