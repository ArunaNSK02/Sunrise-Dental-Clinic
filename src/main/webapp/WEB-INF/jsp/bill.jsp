<%-- pageEncoding declared here, not just in the included header.jsp — see
     manage-appointment.jsp's comment on the same line for why. --%>
<%@ page pageEncoding="UTF-8" %>
<% request.setAttribute("pageTitle", "Bill — Sunrise Dental Clinic"); %>
<%@ include file="/WEB-INF/jspf/header.jsp" %>
    <p class="no-print"><a href="${pageContext.request.contextPath}/dashboard">&larr; Dashboard</a></p>

    <c:if test="${empty bill}">
        <h1>Calculate &amp; Print Bill</h1>
        <c:if test="${not empty error}">
            <div class="alert alert-error">${error}</div>
        </c:if>
        <div class="card">
            <form method="get" action="${pageContext.request.contextPath}/appointments/bill">
                <label>Appointment number
                    <input type="number" name="number" value="${param.number}" required>
                </label>
                <button type="submit">Calculate &amp; Print Bill</button>
            </form>
        </div>
    </c:if>

    <c:if test="${not empty bill}">
        <h1>Receipt</h1>
        <div class="card">
            <table>
                <tr><th>Bill No.</th><td>${bill.billId}</td></tr>
                <tr><th>Appointment No.</th><td>${appointment.appointmentNumber}</td></tr>
                <tr><th>Issue Date</th><td>${bill.issueDate}</td></tr>
                <tr><th>Patient</th><td>${fn:escapeXml(appointment.patient.name)}</td></tr>
                <tr><th>Dentist</th><td>${fn:escapeXml(appointment.dentist.fullName)}</td></tr>
                <tr><th>Treatment</th><td>${fn:escapeXml(appointment.treatment.name)}</td></tr>
                <tr><th>Treatment Cost</th><td>Rs. <fmt:formatNumber value="${bill.treatmentCost}" minFractionDigits="2" maxFractionDigits="2"/></td></tr>
                <tr><th>Consultation Fee</th><td>Rs. <fmt:formatNumber value="${bill.consultationFee}" minFractionDigits="2" maxFractionDigits="2"/></td></tr>
                <tr><th>Total</th><td><strong>Rs. <fmt:formatNumber value="${bill.totalAmount}" minFractionDigits="2" maxFractionDigits="2"/></strong></td></tr>
            </table>
            <div class="actions no-print">
                <button onclick="window.print()">Print</button>
            </div>
        </div>
    </c:if>
<%@ include file="/WEB-INF/jspf/footer.jsp" %>
