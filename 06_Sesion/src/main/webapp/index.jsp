<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="es">
<head><meta charset="UTF-8"><title>Sesiones</title></head>
<body>
<h1>Ejemplo de sesiones</h1>
<p>Identificador de la sesión: <c:out value="${pageContext.session.id}" /></p>
<p>La sesión ya existe. Para acceder al área privada, debes iniciar sesión.</p>
<p>Anota el identificador y compáralo con el que aparece después de iniciar sesión.</p>
<p><a href="${pageContext.request.contextPath}/LoginServlet">Entrar en la aplicación</a></p>
<p>Mostramos el identificador únicamente para esta práctica local.</p>
</body>
</html>
