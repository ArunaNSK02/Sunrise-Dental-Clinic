<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Register New Appointment — Sunrise Dental Clinic</title>
</head>
<body>
    <p><a href="${pageContext.request.contextPath}/dashboard">&larr; Dashboard</a></p>
    <h1>Register New Appointment</h1>

    <c:if test="${not empty error}">
        <p style="color:red;">${error}</p>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/appointments/new">
        <fieldset>
            <legend>Patient</legend>
            <label>Name: <input type="text" name="patientName" value="${param.patientName}" required></label><br>
            <label>Address: <input type="text" name="address" value="${param.address}" required></label><br>
            <label>Contact number: <input type="text" name="contactNumber" value="${param.contactNumber}" required></label><br>
        </fieldset>

        <fieldset>
            <legend>Appointment</legend>
            <label>Dentist:
                <select name="dentistId" required>
                    <option value="">-- choose a dentist --</option>
                    <c:forEach var="dentist" items="${dentists}">
                        <option value="${dentist.dentistId}">${dentist.fullName}</option>
                    </c:forEach>
                </select>
            </label><br>
            <label>Treatment:
                <select name="treatmentId" required>
                    <option value="">-- choose a treatment --</option>
                    <c:forEach var="treatment" items="${treatments}">
                        <option value="${treatment.treatmentId}">${treatment.name} (${treatment.durationMinutes} min, Rs. ${treatment.cost})</option>
                    </c:forEach>
                </select>
            </label><br>
            <label>Date: <input type="date" name="date" required></label><br>
            <label>Time: <input type="time" name="time" required></label><br>
        </fieldset>

        <button type="submit">Register Appointment</button>
    </form>
</body>
</html>
