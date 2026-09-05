<%-- pageEncoding declared here, not just in the included header.jsp — see
     manage-appointment.jsp's comment on the same line for why. --%>
<%@ page pageEncoding="UTF-8" %>
<% request.setAttribute("pageTitle", "Dashboard — Sunrise Dental Clinic"); %>
<%@ include file="/WEB-INF/jspf/header.jsp" %>
    <h1>Welcome, ${fn:escapeXml(sessionScope.loggedInUser.fullName)}</h1>

    <%-- Register New Appointment is never a Dentist's action (decision 8) — see header.jsp's nav for the same rule. --%>
    <c:if test="${not sessionScope.isDentist}">
        <div class="actions" style="margin-bottom:24px;">
            <a class="btn" href="${pageContext.request.contextPath}/appointments/new">+ Register New Appointment</a>
        </div>
    </c:if>

    <h2 style="margin-top:0;">
        <c:choose>
            <c:when test="${sessionScope.isDentist}">Your schedule — ${todayDate}</c:when>
            <c:otherwise>Today's schedule — ${todayDate}</c:otherwise>
        </c:choose>
    </h2>

    <%-- Each row links to Manage — the only path a Dentist has to their own
         appointments, since they don't get standalone Search (decision 6/14:
         Dentist only reaches Search Appointment through Record Appointment
         Delay's own <<include>>, not as a general lookup). Useful for
         Receptionist/Administrator too, not just a Dentist workaround. --%>
    <table>
        <tr>
            <th>Time</th><th>Patient</th><th>Dentist</th><th>Treatment</th><th>Status</th>
        </tr>
        <c:forEach var="appt" items="${todaysAppointments}">
            <tr onclick="location.href='${pageContext.request.contextPath}/appointments/manage?number=${appt.appointmentNumber}'" style="cursor:pointer;">
                <td>${appt.time}</td>
                <td>${fn:escapeXml(appt.patient.name)}</td>
                <td>${fn:escapeXml(appt.dentist.fullName)}</td>
                <td>${fn:escapeXml(appt.treatment.name)}</td>
                <td><span class="badge status-${fn:toLowerCase(appt.status)}">${appt.status}</span></td>
            </tr>
        </c:forEach>
        <c:if test="${empty todaysAppointments}">
            <tr><td colspan="5" class="muted">Nothing scheduled today.</td></tr>
        </c:if>
    </table>
<%@ include file="/WEB-INF/jspf/footer.jsp" %>
