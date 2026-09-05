<%--
    Shared page shell — one place for the <head>, stylesheet link, and top
    navigation bar, statically included (compile-time merge, so taglib
    declarations here apply to the whole combined page) at the top of
    every JSP rather than copy-pasted into each one. A content page sets
    `pageTitle` via a plain scriptlet (available before any taglib is
    declared) immediately before including this file:

        <% request.setAttribute("pageTitle", "Register New Appointment"); %>
        <%@ include file="/WEB-INF/jspf/header.jsp" %>
        ... page content ...
        <%@ include file="/WEB-INF/jspf/footer.jsp" %>

    The nav bar only renders when someone's logged in (sessionScope.loggedInUser)
    so the login page can still use this shell for a consistent look
    without showing internal links to an unauthenticated visitor.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><c:out value="${pageTitle}" default="Sunrise Dental Clinic"/></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<c:if test="${not empty sessionScope.loggedInUser}">
    <div class="topbar">
        <a class="brand" href="${pageContext.request.contextPath}/dashboard">Sunrise Dental Clinic</a>
        <nav>
            <%-- Register/Search/Bill: never for a Dentist (RoleGuard.requireNotDentist
                 enforces this server-side too — decision 8, the use case
                 diagram never associates Dentist with any of these three). --%>
            <c:if test="${not sessionScope.isDentist}">
                <a href="${pageContext.request.contextPath}/appointments/new">Register</a>
                <a href="${pageContext.request.contextPath}/appointments">Search</a>
                <a href="${pageContext.request.contextPath}/appointments/bill">Bill</a>
            </c:if>
            <a href="${pageContext.request.contextPath}/appointments/manage">Manage</a>
            <c:if test="${sessionScope.isDentist or sessionScope.isAdmin}">
                <a href="${pageContext.request.contextPath}/dentist/settings">Availability</a>
            </c:if>
            <c:if test="${sessionScope.isAdmin}">
                <a href="${pageContext.request.contextPath}/staff">Staff</a>
                <a href="${pageContext.request.contextPath}/reports">Reports</a>
            </c:if>
            <a href="${pageContext.request.contextPath}/help">Help</a>
        </nav>
        <div class="user">
            ${sessionScope.loggedInUser.fullName}
            <a href="${pageContext.request.contextPath}/logout">Log out</a>
        </div>
    </div>
</c:if>
<div class="page">
