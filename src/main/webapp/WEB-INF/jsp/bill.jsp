<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Bill — Sunrise Dental Clinic</title>
    <style>
        @media print {
            .no-print { display: none; }
        }
        table { border-collapse: collapse; }
        th, td { text-align: left; padding: 4px 12px; }
    </style>
</head>
<body>
    <div class="no-print">
        <p><a href="${pageContext.request.contextPath}/dashboard">&larr; Dashboard</a></p>
    </div>

    <c:if test="${empty bill}">
        <h1>Calculate &amp; Print Bill</h1>
        <c:if test="${not empty error}">
            <p style="color:red;">${error}</p>
        </c:if>
        <form method="get" action="${pageContext.request.contextPath}/appointments/bill" class="no-print">
            <label>Appointment number: <input type="number" name="number" value="${param.number}" required></label>
            <button type="submit">Calculate &amp; Print Bill</button>
        </form>
    </c:if>

    <c:if test="${not empty bill}">
        <h1>Sunrise Dental Clinic — Receipt</h1>
        <table border="1" cellpadding="6">
            <tr><th>Bill No.</th><td>${bill.billId}</td></tr>
            <tr><th>Appointment No.</th><td>${appointment.appointmentNumber}</td></tr>
            <tr><th>Issue Date</th><td>${bill.issueDate}</td></tr>
            <tr><th>Patient</th><td>${appointment.patient.name}</td></tr>
            <tr><th>Dentist</th><td>${appointment.dentist.fullName}</td></tr>
            <tr><th>Treatment</th><td>${appointment.treatment.name}</td></tr>
            <tr><th>Treatment Cost</th><td>Rs. <fmt:formatNumber value="${bill.treatmentCost}" minFractionDigits="2" maxFractionDigits="2"/></td></tr>
            <tr><th>Consultation Fee</th><td>Rs. <fmt:formatNumber value="${bill.consultationFee}" minFractionDigits="2" maxFractionDigits="2"/></td></tr>
            <tr><th><strong>Total</strong></th><td><strong>Rs. <fmt:formatNumber value="${bill.totalAmount}" minFractionDigits="2" maxFractionDigits="2"/></strong></td></tr>
        </table>

        <p class="no-print"><button onclick="window.print()">Print</button></p>
    </c:if>
</body>
</html>
