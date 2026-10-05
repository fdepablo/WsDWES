<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Registro completado</title>
</head>
<body>
<main>
    <h1>Usuario registrado</h1>

    <p><strong>Nombre:</strong> <c:out value="${usuario.nombre}"/></p>
    <p><strong>Correo:</strong> <c:out value="${usuario.email}"/></p>
    <p><strong>Tipo de cuenta:</strong> <c:out value="${usuario.tipoCuenta}"/></p>

    <p><strong>Intereses:</strong></p>
    <c:choose>
        <c:when test="${empty usuario.intereses}">
            <p>Ninguno</p>
        </c:when>
        <c:otherwise>
            <ul>
                <c:forEach items="${usuario.intereses}" var="interes">
                    <li><c:out value="${interes}"/></li>
                </c:forEach>
            </ul>
        </c:otherwise>
    </c:choose>

    <p>Usuarios registrados durante esta ejecución: <c:out value="${totalUsuarios}"/></p>
    <p><a href="${pageContext.request.contextPath}/formulario">Volver al formulario</a></p>
</main>
</body>
</html>
