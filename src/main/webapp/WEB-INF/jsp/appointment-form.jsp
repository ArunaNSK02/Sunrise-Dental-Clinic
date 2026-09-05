<%-- pageEncoding declared here, not just in the included header.jsp — see
     manage-appointment.jsp's comment on the same line for why. --%>
<%@ page pageEncoding="UTF-8" %>
<% request.setAttribute("pageTitle", "Register New Appointment — Sunrise Dental Clinic"); %>
<%@ include file="/WEB-INF/jspf/header.jsp" %>
    <p><a href="${pageContext.request.contextPath}/dashboard">&larr; Dashboard</a></p>
    <h1>Register New Appointment</h1>

    <c:if test="${not empty error}">
        <div class="alert alert-error">${error}</div>
    </c:if>

    <div class="card">
        <form method="post" action="${pageContext.request.contextPath}/appointments/new">
            <fieldset>
                <legend>Patient</legend>
                <label>Name
                    <input type="text" name="patientName" value="${fn:escapeXml(param.patientName)}" required>
                </label>
                <label>Address
                    <input type="text" name="address" value="${fn:escapeXml(param.address)}" required>
                </label>
                <label>Contact number
                    <input type="text" name="contactNumber" value="${fn:escapeXml(param.contactNumber)}" required>
                </label>
            </fieldset>

            <fieldset>
                <legend>Appointment</legend>
                <label>Dentist
                    <select name="dentistId" required>
                        <option value="">-- choose a dentist --</option>
                        <c:forEach var="dentist" items="${dentists}">
                            <option value="${dentist.dentistId}">${fn:escapeXml(dentist.fullName)}</option>
                        </c:forEach>
                    </select>
                </label>
                <label>Treatment
                    <select name="treatmentId" required>
                        <option value="">-- choose a treatment --</option>
                        <c:forEach var="treatment" items="${treatments}">
                            <option value="${treatment.treatmentId}">${treatment.name} (${treatment.durationMinutes} min, Rs. ${treatment.cost})</option>
                        </c:forEach>
                    </select>
                </label>
                <label>Date
                    <input type="date" name="date" required>
                </label>
                <label>Time
                    <input type="time" name="time" required>
                </label>
            </fieldset>

            <button type="submit">Register Appointment</button>
        </form>
    </div>
<%@ include file="/WEB-INF/jspf/footer.jsp" %>
