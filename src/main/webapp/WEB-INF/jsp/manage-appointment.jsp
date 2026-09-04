<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Manage Appointment — Sunrise Dental Clinic</title>
    <style>fieldset { margin-bottom: 16px; }</style>
</head>
<body>
    <p><a href="${pageContext.request.contextPath}/dashboard">&larr; Dashboard</a></p>
    <h1>Cancel / Delay / Reschedule Appointment</h1>

    <c:if test="${empty appointment}">
        <c:if test="${not empty notFoundNumber}">
            <p style="color:red;">No appointment found with number ${notFoundNumber}.</p>
        </c:if>
        <c:if test="${not empty error}">
            <p style="color:red;">${error}</p>
        </c:if>
        <form method="get" action="${pageContext.request.contextPath}/appointments/manage">
            <label>Appointment number: <input type="number" name="number" required></label>
            <button type="submit">Look up</button>
        </form>
    </c:if>

    <c:if test="${not empty appointment}">
        <c:if test="${not empty message}">
            <p style="color:green;">${message}</p>
        </c:if>
        <c:if test="${not empty error}">
            <p style="color:red;">${error}</p>
        </c:if>

        <table border="1" cellpadding="6">
            <tr><th>Appointment Number</th><td>${appointment.appointmentNumber}</td></tr>
            <tr><th>Patient</th><td>${appointment.patient.name}</td></tr>
            <tr><th>Dentist</th><td>${appointment.dentist.fullName}</td></tr>
            <tr><th>Treatment</th><td>${appointment.treatment.name}</td></tr>
            <tr><th>Date</th><td>${appointment.date}</td></tr>
            <tr><th>Time</th><td>${appointment.time}</td></tr>
            <tr><th>Status</th><td>${appointment.status}</td></tr>
            <tr><th>Delay so far</th><td>${appointment.delayMinutes} min</td></tr>
        </table>

        <c:set var="number" value="${appointment.appointmentNumber}"/>
        <c:set var="isCancelled" value="${appointment.status == 'CANCELLED'}"/>

        <fieldset>
            <legend>Cancel Appointment</legend>
            <form method="post" action="${pageContext.request.contextPath}/appointments/manage">
                <input type="hidden" name="number" value="${number}">
                <input type="hidden" name="action" value="cancel">
                <label><input type="radio" name="reason" value="PATIENT" checked> Patient-requested</label>
                <label><input type="radio" name="reason" value="DENTIST"> Dentist-unavailable</label>
                <button type="submit" ${isCancelled ? 'disabled' : ''}>Cancel Appointment</button>
            </form>
        </fieldset>

        <fieldset>
            <legend>Record Appointment Delay</legend>
            <form method="post" action="${pageContext.request.contextPath}/appointments/manage" id="delayForm">
                <input type="hidden" name="number" value="${number}">
                <input type="hidden" name="action" value="delay">
                <label>Delay (minutes): <input type="number" name="minutes" min="1" required></label><br>
                <label><input type="radio" name="who" value="dentist" checked onclick="document.getElementById('waitSkip').style.display='none'">
                    Dentist-caused — cascades automatically to the rest of the day</label><br>
                <label><input type="radio" name="who" value="patient" onclick="document.getElementById('waitSkip').style.display='inline'">
                    Patient-caused — needs a Wait/Skip decision</label>
                <span id="waitSkip" style="display:none;">
                    <label><input type="radio" name="decision" value="WAIT" checked> Wait (cascades like a dentist delay)</label>
                    <label><input type="radio" name="decision" value="SKIP"> Skip (cancels this slot — reschedule below)</label>
                </span><br>
                <button type="submit" ${isCancelled ? 'disabled' : ''}>Record Delay</button>
            </form>
        </fieldset>

        <fieldset>
            <legend>Reschedule Appointment</legend>
            <form method="post" action="${pageContext.request.contextPath}/appointments/manage">
                <input type="hidden" name="number" value="${number}">
                <input type="hidden" name="action" value="reschedule">
                <label>New date: <input type="date" name="newDate" required></label>
                <label>New time: <input type="time" name="newTime" required></label>
                <button type="submit" ${isCancelled ? 'disabled' : ''}>Reschedule</button>
            </form>
        </fieldset>
    </c:if>
</body>
</html>
