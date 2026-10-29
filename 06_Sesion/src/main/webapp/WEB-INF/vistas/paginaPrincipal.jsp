<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="es">
<head><meta charset="UTF-8"><title>Inicio privado</title></head>
<body>
<h1>Área privada</h1>
<p>Usuario de la sesión: <c:out value="${sessionScope.usuario.nombre}" /> </p>
<p>Identificador después del login: <c:out value="${pageContext.session.id}" /></p>
<p>Si anotaste el identificador antes de entrar, verás que ha cambiado por la llamada a <code>changeSessionId()</code>.</p>
<p>Mostramos el identificador únicamente para esta práctica local. No se debería mostrar nunca</p>

<p><a href="${pageContext.request.contextPath}/UsuariosServlet">Ver usuarios</a></p>

<form action="${pageContext.request.contextPath}/LogoutServlet" method="post">
    <button type="submit">Cerrar sesión</button>
</form>

</body>
</html>
