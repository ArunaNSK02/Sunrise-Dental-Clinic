<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Display Appointment Details — Sunrise Dental Clinic</title>
</head>
<body>
    <p><a href="${pageContext.request.contextPath}/dashboard">&larr; Dashboard</a></p>
    <h1>Display Appointment Details</h1>

    <form method="get" action="${pageContext.request.contextPath}/appointments">
        <label>Appointment number: <input type="number" name="number" value="${param.number}" required></label>
        <button type="submit">Search</button>
    </form>

    <c:if test="${not empty error}">
        <p style="color:red;">${error}</p>
    </c:if>

    <c:if test="${not empty notFoundNumber}">
        <p style="color:red;">No appointment found with number ${notFoundNumber}.</p>
    </c:if>

    <c:if test="${not empty appointment}">
        <c:if test="${appointment.appointmentNumber == param.number}">
            <p style="color:green;">Appointment booked successfully.</p>
        </c:if>
        <table border="1" cellpadding="6">
            <tr><th>Appointment Number</th><td>${appointment.appointmentNumber}</td></tr>
            <tr><th>Patient</th><td>${appointment.patient.name}</td></tr>
            <tr><th>Address</th><td>${appointment.patient.address}</td></tr>
            <tr><th>Contact Number</th><td>${appointment.patient.contactNumber}</td></tr>
            <tr><th>Dentist</th><td>${appointment.dentist.fullName}</td></tr>
            <tr><th>Treatment</th><td>${appointment.treatment.name}</td></tr>
            <tr><th>Date</th><td>${appointment.date}</td></tr>
            <tr><th>Time</th><td>${appointment.time}</td></tr>
            <tr><th>Status</th><td>${appointment.status}</td></tr>
        </table>
        <p><a href="${pageContext.request.contextPath}/appointments/bill?number=${appointment.appointmentNumber}">Calculate &amp; Print Bill</a></p>
    </c:if>
</body>
</html>
