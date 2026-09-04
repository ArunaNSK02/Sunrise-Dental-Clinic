<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Dentist Availability &amp; Capacity — Sunrise Dental Clinic</title>
</head>
<body>
    <p><a href="${pageContext.request.contextPath}/dashboard">&larr; Dashboard</a></p>
    <h1>Set Daily Appointment Limit / Availability</h1>

    <c:if test="${not empty error}">
        <p style="color:red;">${error}</p>
    </c:if>

    <c:if test="${not empty dentists}">
        <p>Choose a dentist to manage:</p>
        <ul>
            <c:forEach var="d" items="${dentists}">
                <li><a href="${pageContext.request.contextPath}/dentist/settings?dentistId=${d.dentistId}">${d.fullName}</a>
                    (limit: ${d.dailyAppointmentLimit})</li>
            </c:forEach>
        </ul>
    </c:if>

    <c:if test="${not empty dentist}">
        <h2>${dentist.fullName}</h2>

        <fieldset>
            <legend>Daily Appointment Limit (currently ${dentist.dailyAppointmentLimit})</legend>
            <form method="post" action="${pageContext.request.contextPath}/dentist/settings">
                <input type="hidden" name="dentistId" value="${dentist.dentistId}">
                <input type="hidden" name="action" value="limit">
                <label>New limit: <input type="number" name="limit" min="1" required></label>
                <button type="submit">Update</button>
            </form>
        </fieldset>

        <fieldset>
            <legend>Mark a period unavailable</legend>
            <form method="post" action="${pageContext.request.contextPath}/dentist/settings">
                <input type="hidden" name="dentistId" value="${dentist.dentistId}">
                <input type="hidden" name="action" value="availability">
                <label>From: <input type="datetime-local" name="start" required></label>
                <label>To: <input type="datetime-local" name="end" required></label>
                <label>Reason: <input type="text" name="reason"></label>
                <button type="submit">Add</button>
            </form>
        </fieldset>

        <h3>Current unavailable periods</h3>
        <table border="1" cellpadding="6">
            <tr><th>From</th><th>To</th><th>Reason</th></tr>
            <c:forEach var="block" items="${dentist.unavailablePeriods}">
                <tr><td>${block.startDateTime}</td><td>${block.endDateTime}</td><td>${block.reason}</td></tr>
            </c:forEach>
        </table>
    </c:if>
</body>
</html>
