<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>No se pudo completar el registro</title>
</head>
<body>
<main>
    <h1>No se pudo completar el registro</h1>
    <p><c:out value="${mensajeError}"/></p>
    <p><a href="${pageContext.request.contextPath}/registro">Volver al formulario</a></p>
</main>
</body>
</html>
