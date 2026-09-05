<%-- pageEncoding must be declared in THIS file (the one Tomcat compiles as
     the translation unit's top-level source), not just in header.jsp's own
     page directive — Jasper decodes each physical file's bytes as it reads
     them, and by the time the merged translation unit reaches header.jsp's
     directive (via the static include below) this file's own em-dash text
     above has already been mis-decoded using the JVM's platform-default
     charset (Cp1252 on this Windows/JDK17 setup, since JEP 400's UTF-8
     default only starts at JDK 18). Found via the end-to-end UI test run
     2026-09-05 — every em dash on this page rendered as "â€"" mojibake
     until this line was added; adding pageEncoding to header.jsp alone
     did not fix it, confirming the per-top-level-file requirement. --%>
<%@ page pageEncoding="UTF-8" %>
<% request.setAttribute("pageTitle", "Cancel / Delay / Reschedule — Sunrise Dental Clinic"); %>
<%@ include file="/WEB-INF/jspf/header.jsp" %>
    <p><a href="${pageContext.request.contextPath}/dashboard">&larr; Dashboard</a></p>
    <h1>Cancel / Delay / Reschedule Appointment</h1>

    <c:if test="${empty appointment}">
        <c:if test="${not empty notFoundNumber}">
            <div class="alert alert-error">No appointment found with number ${notFoundNumber}.</div>
        </c:if>
        <c:if test="${not empty error}">
            <div class="alert alert-error">${error}</div>
        </c:if>
        <div class="card">
            <form method="get" action="${pageContext.request.contextPath}/appointments/manage">
                <label>Appointment number
                    <input type="number" name="number" required>
                </label>
                <button type="submit">Look up</button>
            </form>
        </div>
    </c:if>

    <c:if test="${not empty appointment}">
        <c:if test="${not empty message}">
            <div class="alert alert-success">${message}</div>
        </c:if>
        <c:if test="${not empty error}">
            <div class="alert alert-error">${error}</div>
        </c:if>

        <div class="card">
            <table>
                <tr><th>Appointment Number</th><td>${appointment.appointmentNumber}</td></tr>
                <tr><th>Patient</th><td>${fn:escapeXml(appointment.patient.name)}</td></tr>
                <tr><th>Dentist</th><td>${fn:escapeXml(appointment.dentist.fullName)}</td></tr>
                <tr><th>Treatment</th><td>${fn:escapeXml(appointment.treatment.name)}</td></tr>
                <tr><th>Date</th><td>${appointment.date}</td></tr>
                <tr><th>Time</th><td>${appointment.time}</td></tr>
                <tr><th>Status</th><td><span class="badge status-${fn:toLowerCase(appointment.status)}">${appointment.status}</span></td></tr>
                <tr><th>Delay so far</th><td>${appointment.delayMinutes} min</td></tr>
            </table>
        </div>

        <c:set var="number" value="${appointment.appointmentNumber}"/>
        <c:set var="isCancelled" value="${appointment.status == 'CANCELLED'}"/>

        <div class="card">
            <%-- Cancel: never a Dentist's action (decision 8 — billing
                 consequences, stays front-desk). Hidden here, not just
                 disabled — ManageAppointmentServlet also rejects a
                 hand-crafted cancel POST from a Dentist server-side, so
                 this is UI politeness on top of real enforcement, not
                 the only line of defense. --%>
            <c:if test="${not sessionScope.isDentist}">
                <fieldset>
                    <legend>Cancel Appointment</legend>
                    <form method="post" action="${pageContext.request.contextPath}/appointments/manage">
                        <input type="hidden" name="number" value="${number}">
                        <input type="hidden" name="action" value="cancel">
                        <div class="radio-row">
                            <label class="inline"><input type="radio" name="reason" value="PATIENT" checked> Patient-requested</label>
                            <label class="inline"><input type="radio" name="reason" value="DENTIST"> Dentist-unavailable</label>
                        </div>
                        <button type="submit" ${isCancelled ? 'disabled' : ''}>Cancel Appointment</button>
                    </form>
                </fieldset>
            </c:if>

            <fieldset>
                <legend>Record Appointment Delay</legend>
                <form method="post" action="${pageContext.request.contextPath}/appointments/manage">
                    <input type="hidden" name="number" value="${number}">
                    <input type="hidden" name="action" value="delay">
                    <label>Delay (minutes)
                        <input type="number" name="minutes" min="1" required style="max-width:120px;">
                    </label>
                    <%-- A Dentist recording a delay is always reporting their
                         own (decision 10 — no decision step, always
                         cascades); the Wait/Skip choice (decision 11) only
                         makes sense for a Receptionist observing a late
                         patient, so it's skipped entirely for a Dentist
                         rather than shown and ignored. --%>
                    <c:choose>
                        <c:when test="${sessionScope.isDentist}">
                            <input type="hidden" name="who" value="dentist">
                            <p class="muted">Recorded as your own delay — cascades automatically to the rest of today's schedule.</p>
                        </c:when>
                        <c:otherwise>
                            <div class="radio-row">
                                <label class="inline">
                                    <input type="radio" name="who" value="dentist" checked onclick="document.getElementById('waitSkip').style.display='none'">
                                    Dentist-caused — cascades automatically to the rest of the day
                                </label><br>
                                <label class="inline">
                                    <input type="radio" name="who" value="patient" onclick="document.getElementById('waitSkip').style.display='inline-flex'">
                                    Patient-caused — needs a Wait/Skip decision
                                </label>
                            </div>
                            <div id="waitSkip" class="radio-row" style="display:none;">
                                <label class="inline"><input type="radio" name="decision" value="WAIT" checked> Wait (cascades like a dentist delay)</label>
                                <label class="inline"><input type="radio" name="decision" value="SKIP"> Skip (cancels this slot — reschedule below)</label>
                            </div>
                        </c:otherwise>
                    </c:choose>
                    <button type="submit" ${isCancelled ? 'disabled' : ''}>Record Delay</button>
                </form>
            </fieldset>

            <fieldset>
                <legend>Reschedule Appointment</legend>
                <form method="post" action="${pageContext.request.contextPath}/appointments/manage">
                    <input type="hidden" name="number" value="${number}">
                    <input type="hidden" name="action" value="reschedule">
                    <label>New date
                        <input type="date" name="newDate" required>
                    </label>
                    <label>New time
                        <input type="time" name="newTime" required>
                    </label>
                    <button type="submit" ${isCancelled ? 'disabled' : ''}>Reschedule</button>
                </form>
            </fieldset>
        </div>

        <c:if test="${not empty notifications}">
            <h2>Notifications sent</h2>
            <table>
                <tr><th>When</th><th>Channel</th><th>To</th><th>Message</th></tr>
                <c:forEach var="n" items="${notifications}">
                    <tr>
                        <td>${n.sentAt}</td>
                        <td>${fn:escapeXml(n.channel)}</td>
                        <td>${fn:escapeXml(n.recipient)}</td>
                        <td>${fn:escapeXml(n.message)}</td>
                    </tr>
                </c:forEach>
            </table>
        </c:if>
    </c:if>
<%@ include file="/WEB-INF/jspf/footer.jsp" %>
