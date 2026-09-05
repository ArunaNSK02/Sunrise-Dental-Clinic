<%-- pageEncoding declared here, not just in the included header.jsp — see
     manage-appointment.jsp's comment on the same line for why. --%>
<%@ page pageEncoding="UTF-8" %>
<% request.setAttribute("pageTitle", "Display Appointment Details — Sunrise Dental Clinic"); %>
<%@ include file="/WEB-INF/jspf/header.jsp" %>
    <p><a href="${pageContext.request.contextPath}/dashboard">&larr; Dashboard</a></p>
    <h1>Display Appointment Details</h1>

    <div class="card">
        <form method="get" action="${pageContext.request.contextPath}/appointments">
            <label>Appointment number
                <input type="number" name="number" value="${param.number}" required>
            </label>
            <button type="submit">Search</button>
        </form>
    </div>

    <c:if test="${not empty error}">
        <div class="alert alert-error">${error}</div>
    </c:if>

    <c:if test="${not empty notFoundNumber}">
        <div class="alert alert-error">No appointment found with number ${notFoundNumber}.</div>
    </c:if>

    <c:if test="${not empty appointment}">
        <c:if test="${appointment.appointmentNumber == param.number}">
            <div class="alert alert-success">Appointment booked successfully.</div>
        </c:if>
        <div class="card">
            <table>
                <tr><th>Appointment Number</th><td>${appointment.appointmentNumber}</td></tr>
                <tr><th>Patient</th><td>${fn:escapeXml(appointment.patient.name)}</td></tr>
                <tr><th>Address</th><td>${fn:escapeXml(appointment.patient.address)}</td></tr>
                <tr><th>Contact Number</th><td>${fn:escapeXml(appointment.patient.contactNumber)}</td></tr>
                <tr><th>Dentist</th><td>${fn:escapeXml(appointment.dentist.fullName)}</td></tr>
                <tr><th>Treatment</th><td>${fn:escapeXml(appointment.treatment.name)}</td></tr>
                <tr><th>Date</th><td>${appointment.date}</td></tr>
                <tr><th>Time</th><td>${appointment.time}</td></tr>
                <tr><th>Status</th><td><span class="badge status-${fn:toLowerCase(appointment.status)}">${appointment.status}</span></td></tr>
            </table>
            <div class="actions">
                <a class="btn" href="${pageContext.request.contextPath}/appointments/bill?number=${appointment.appointmentNumber}">Calculate &amp; Print Bill</a>
                <a class="btn secondary" href="${pageContext.request.contextPath}/appointments/manage?number=${appointment.appointmentNumber}">Cancel / Delay / Reschedule</a>
            </div>
        </div>
    </c:if>
<%@ include file="/WEB-INF/jspf/footer.jsp" %>
