<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="es">
<head><meta charset="UTF-8"><title>Personas con JPA</title></head>
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
                            <a href="${pageContext.request.contextPath}/personas/editar?id=${persona.id}">Editar</a>
                            <form action="${pageContext.request.contextPath}/personas/eliminar" method="post" style="display:inline">
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
