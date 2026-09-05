<%-- pageEncoding declared here, not just in the included header.jsp — see
     manage-appointment.jsp's comment on the same line for why. --%>
<%@ page pageEncoding="UTF-8" %>
<% request.setAttribute("pageTitle", "Reports — Sunrise Dental Clinic"); %>
<%@ include file="/WEB-INF/jspf/header.jsp" %>
    <p><a href="${pageContext.request.contextPath}/dashboard">&larr; Dashboard</a></p>
    <h1>Clinic Reports</h1>

    <div class="stat-grid">
        <div class="stat-tile">
            <div class="value">Rs. <fmt:formatNumber value="${totalRevenue}" minFractionDigits="0" maxFractionDigits="0"/></div>
            <div class="label">Total revenue billed</div>
        </div>
        <c:forEach var="entry" items="${statusCounts}">
            <div class="stat-tile">
                <div class="value">${entry.value}</div>
                <div class="label">${entry.key}</div>
            </div>
        </c:forEach>
    </div>

    <h2>Appointment load by dentist</h2>
    <div class="card">
        <form method="get" action="${pageContext.request.contextPath}/reports">
            <label>Date
                <input type="date" name="date" value="${reportDate}">
            </label>
            <button type="submit">Show</button>
        </form>
    </div>

    <table>
        <tr><th>Dentist</th><th>Appointments on ${reportDate}</th><th>Revenue billed for that date</th></tr>
        <c:forEach var="load" items="${dentistLoad}">
            <tr>
                <td>${fn:escapeXml(load.dentistName)}</td>
                <td>${load.appointmentCount}</td>
                <td>Rs. <fmt:formatNumber value="${load.revenue}" minFractionDigits="2" maxFractionDigits="2"/></td>
            </tr>
        </c:forEach>
        <c:if test="${empty dentistLoad}">
            <tr><td colspan="3" class="muted">No appointments on this date.</td></tr>
        </c:if>
    </table>
<%@ include file="/WEB-INF/jspf/footer.jsp" %>
