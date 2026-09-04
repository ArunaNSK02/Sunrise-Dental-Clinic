<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Reports — Sunrise Dental Clinic</title>
</head>
<body>
    <p><a href="${pageContext.request.contextPath}/dashboard">&larr; Dashboard</a></p>
    <h1>Clinic Reports</h1>

    <h2>Appointments by status (clinic-wide, all time)</h2>
    <table border="1" cellpadding="6">
        <tr><th>Status</th><th>Count</th></tr>
        <c:forEach var="entry" items="${statusCounts}">
            <tr><td>${entry.key}</td><td>${entry.value}</td></tr>
        </c:forEach>
    </table>

    <h2>Total revenue billed</h2>
    <p>Rs. <fmt:formatNumber value="${totalRevenue}" minFractionDigits="2" maxFractionDigits="2"/></p>

    <h2>Appointment load by dentist</h2>
    <form method="get" action="${pageContext.request.contextPath}/reports">
        <label>Date: <input type="date" name="date" value="${reportDate}"></label>
        <button type="submit">Show</button>
    </form>
    <table border="1" cellpadding="6">
        <tr><th>Dentist</th><th>Appointments on ${reportDate}</th><th>Revenue billed for that date</th></tr>
        <c:forEach var="load" items="${dentistLoad}">
            <tr>
                <td>${load.dentistName}</td>
                <td>${load.appointmentCount}</td>
                <td>Rs. <fmt:formatNumber value="${load.revenue}" minFractionDigits="2" maxFractionDigits="2"/></td>
            </tr>
        </c:forEach>
        <c:if test="${empty dentistLoad}">
            <tr><td colspan="3">No appointments on this date.</td></tr>
        </c:if>
    </table>
</body>
</html>
