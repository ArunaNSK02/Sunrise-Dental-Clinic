<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Manage Staff Accounts — Sunrise Dental Clinic</title>
</head>
<body>
    <p><a href="${pageContext.request.contextPath}/dashboard">&larr; Dashboard</a></p>
    <h1>Manage Staff Accounts</h1>

    <c:if test="${not empty error}">
        <p style="color:red;">${error}</p>
    </c:if>

    <table border="1" cellpadding="6">
        <tr><th>User ID</th><th>Username</th><th>Full Name</th><th>Role</th><th></th></tr>
        <c:forEach var="user" items="${staff}">
            <tr>
                <td>${user.userId}</td>
                <td>${user.username}</td>
                <td>${user.fullName}</td>
                <td>${user.role}</td>
                <td>
                    <form method="post" action="${pageContext.request.contextPath}/staff" style="display:inline;">
                        <input type="hidden" name="action" value="remove">
                        <input type="hidden" name="userId" value="${user.userId}">
                        <button type="submit" onclick="return confirm('Remove this staff account?');">Remove</button>
                    </form>
                </td>
            </tr>
        </c:forEach>
    </table>

    <h2>Add Staff Account</h2>
    <form method="post" action="${pageContext.request.contextPath}/staff">
        <input type="hidden" name="action" value="add">
        <label>Username: <input type="text" name="username" required></label><br>
        <label>Password: <input type="password" name="password" required></label><br>
        <label>Full name: <input type="text" name="fullName" required></label><br>
        <label>Role:
            <select name="role" required>
                <option value="RECEPTIONIST">Receptionist</option>
                <option value="DENTIST">Dentist</option>
                <option value="ADMINISTRATOR">Administrator</option>
            </select>
        </label><br>
        <button type="submit">Add</button>
    </form>
</body>
</html>
