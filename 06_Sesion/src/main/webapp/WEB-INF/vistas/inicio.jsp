<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="es">
<head><meta charset="UTF-8"><title>Inicio privado</title></head>
<body>
<h1>Área privada</h1>
<p>Usuario de la sesión: <c:out value="${sessionScope.usuario.nombre}" /></p>
<p><a href="${pageContext.request.contextPath}/usuarios">Ver usuarios</a></p>
<form action="${pageContext.request.contextPath}/logout" method="post">
    <button type="submit">Cerrar sesión</button>
</form>
</body>
</html>
