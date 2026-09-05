<%-- pageEncoding declared here, not just in the included header.jsp — see
     manage-appointment.jsp's comment on the same line for why. --%>
<%@ page pageEncoding="UTF-8" %>
<% request.setAttribute("pageTitle", "Manage Staff Accounts — Sunrise Dental Clinic"); %>
<%@ include file="/WEB-INF/jspf/header.jsp" %>
    <p><a href="${pageContext.request.contextPath}/dashboard">&larr; Dashboard</a></p>
    <h1>Manage Staff Accounts</h1>

    <c:if test="${not empty error}">
        <div class="alert alert-error">${error}</div>
    </c:if>

    <table>
        <tr><th>User ID</th><th>Username</th><th>Full Name</th><th>Role</th><th></th></tr>
        <c:forEach var="user" items="${staff}">
            <tr>
                <td>${user.userId}</td>
                <td>${fn:escapeXml(user.username)}</td>
                <td>${fn:escapeXml(user.fullName)}</td>
                <td>${user.role}</td>
                <td>
                    <form method="post" action="${pageContext.request.contextPath}/staff" style="display:inline;">
                        <input type="hidden" name="action" value="remove">
                        <input type="hidden" name="userId" value="${user.userId}">
                        <button type="submit" class="danger btn-sm" onclick="return confirm('Remove this staff account?');">Remove</button>
                    </form>
                </td>
            </tr>
        </c:forEach>
    </table>

    <h2>Add Staff Account</h2>
    <div class="card">
        <form method="post" action="${pageContext.request.contextPath}/staff">
            <input type="hidden" name="action" value="add">
            <label>Username
                <input type="text" name="username" required>
            </label>
            <label>Password
                <input type="password" name="password" required>
            </label>
            <label>Full name
                <input type="text" name="fullName" required>
            </label>
            <label>Role
                <select name="role" required>
                    <option value="RECEPTIONIST">Receptionist</option>
                    <option value="DENTIST">Dentist</option>
                    <option value="ADMINISTRATOR">Administrator</option>
                </select>
            </label>
            <button type="submit">Add</button>
        </form>
    </div>
<%@ include file="/WEB-INF/jspf/footer.jsp" %>
