<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="es">
<head><meta charset="UTF-8"><title>Listado de usuarios</title></head>
<body>
<h1>Usuarios</h1>
<p>Sesión de <c:out value="${sessionScope.usuario.nombre}" /></p>
<table>
    <thead><tr><th scope="col">ID</th><th scope="col">Nombre</th></tr></thead>
    <tbody>
    <c:forEach var="usuario" items="${requestScope.usuarios}">
        <tr><td><c:out value="${usuario.id}" /></td><td><c:out value="${usuario.nombre}" /></td></tr>
    </c:forEach>
    </tbody>
</table>
<p><a href="${pageContext.request.contextPath}/inicio">Volver</a></p>
<form action="${pageContext.request.contextPath}/logout" method="post">
    <button type="submit">Cerrar sesión</button>
</form>
</body>
</html>
