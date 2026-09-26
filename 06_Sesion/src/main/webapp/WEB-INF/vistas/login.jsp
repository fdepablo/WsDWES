<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="es">
<head><meta charset="UTF-8"><title>Iniciar sesión</title></head>
<body>
<h1>Iniciar sesión</h1>
<c:if test="${not empty requestScope.error}">
    <p role="alert"><c:out value="${requestScope.error}" /></p>
</c:if>
<form action="${pageContext.request.contextPath}/login" method="post">
    <label>Nombre <input name="nombre" required autocomplete="username"></label>
    <label>Contraseña <input type="password" name="password" required autocomplete="current-password"></label>
    <button type="submit">Entrar</button>
</form>
</body>
</html>
