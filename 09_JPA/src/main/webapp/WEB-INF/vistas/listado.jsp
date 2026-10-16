<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Personas con JPA</title>
    <style>
        table { border-collapse: collapse; }
        th, td { border: 1px solid gray; padding: 8px; }
        form { display: inline; }
    </style>
</head>
<body>
<main>
    <h1>Personas con JPA</h1>
    <p><a href="${pageContext.request.contextPath}/personas/nuevo">Añadir persona</a></p>
    <c:choose>
        <c:when test="${empty requestScope.personas}"><p>Todavía no hay personas.</p></c:when>
        <c:otherwise>
            <table>
                <thead><tr><th scope="col">ID</th><th scope="col">Nombre</th><th scope="col">Edad</th><th scope="col">Peso (kg)</th><th scope="col">Acciones</th></tr></thead>
                <tbody>
                <c:forEach var="persona" items="${requestScope.personas}">
                    <tr>
                        <td><c:out value="${persona.id}" /></td>
                        <td><c:out value="${persona.nombre}" /></td>
                        <td><c:out value="${persona.edad}" /></td>
                        <td><c:out value="${persona.peso}" /></td>
                        <td>
                            <%-- GET abre la edición y envía el ID como parámetro de la URL. --%>
                            <form action="${pageContext.request.contextPath}/personas/editar" method="get">
                                <input type="hidden" name="id" value="${persona.id}">
                                <button type="submit">Editar</button>
                            </form>
                            <%-- El borrado modifica datos: se envía por POST. --%>
                            <form action="${pageContext.request.contextPath}/personas/eliminar" method="post">
                                <input type="hidden" name="id" value="${persona.id}">
                                <button type="submit">Eliminar</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </c:otherwise>
    </c:choose>
</main>
</body>
</html>
