<%-- pageEncoding declared here, not just in the included header.jsp — see
     manage-appointment.jsp's comment on the same line for why. --%>
<%@ page pageEncoding="UTF-8" %>
<% request.setAttribute("pageTitle", "Dentist Availability & Capacity — Sunrise Dental Clinic"); %>
<%@ include file="/WEB-INF/jspf/header.jsp" %>
    <p><a href="${pageContext.request.contextPath}/dashboard">&larr; Dashboard</a></p>
    <h1>Set Daily Appointment Limit / Availability</h1>

    <c:if test="${not empty error}">
        <div class="alert alert-error">${error}</div>
    </c:if>

    <c:if test="${not empty dentists}">
        <p class="muted">Choose a dentist to manage:</p>
        <div class="menu-grid">
            <c:forEach var="d" items="${dentists}">
                <a href="${pageContext.request.contextPath}/dentist/settings?dentistId=${d.dentistId}">
                    ${fn:escapeXml(d.fullName)}
                    <span class="desc">Daily limit: ${d.dailyAppointmentLimit}</span>
                </a>
            </c:forEach>
        </div>
    </c:if>

    <c:if test="${not empty dentist}">
        <h2>${fn:escapeXml(dentist.fullName)}</h2>

        <div class="card">
            <fieldset>
                <legend>Daily Appointment Limit (currently ${dentist.dailyAppointmentLimit})</legend>
                <form method="post" action="${pageContext.request.contextPath}/dentist/settings">
                    <input type="hidden" name="dentistId" value="${dentist.dentistId}">
                    <input type="hidden" name="action" value="limit">
                    <label>New limit
                        <input type="number" name="limit" min="1" required style="max-width:120px;">
                    </label>
                    <button type="submit">Update</button>
                </form>
            </fieldset>

            <fieldset>
                <legend>Mark a period unavailable</legend>
                <form method="post" action="${pageContext.request.contextPath}/dentist/settings">
                    <input type="hidden" name="dentistId" value="${dentist.dentistId}">
                    <input type="hidden" name="action" value="availability">
                    <label>From
                        <input type="datetime-local" name="start" required>
                    </label>
                    <label>To
                        <input type="datetime-local" name="end" required>
                    </label>
                    <label>Reason
                        <input type="text" name="reason">
                    </label>
                    <button type="submit">Add</button>
                </form>
            </fieldset>
        </div>

        <h2>Current unavailable periods</h2>
        <table>
            <tr><th>From</th><th>To</th><th>Reason</th></tr>
            <c:forEach var="block" items="${dentist.unavailablePeriods}">
                <tr><td>${block.startDateTime}</td><td>${block.endDateTime}</td><td>${fn:escapeXml(block.reason)}</td></tr>
            </c:forEach>
            <c:if test="${empty dentist.unavailablePeriods}">
                <tr><td colspan="3" class="muted">None recorded.</td></tr>
            </c:if>
        </table>
    </c:if>
<%@ include file="/WEB-INF/jspf/footer.jsp" %>
