<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Productos</title>
    <style>
        table { border-collapse: collapse; }
        th, td { border: 1px solid gray; padding: 8px; }
        form { display: inline; }
    </style>
</head>
<body>
<main>
    <h1>Productos</h1>
    <p><a href="${pageContext.request.contextPath}/productos/nuevo">Añadir producto</a></p>
    <c:choose>
        <c:when test="${empty requestScope.productos}">
            <p>Todavía no hay productos.</p>
        </c:when>
        <c:otherwise>
            <table>
                <thead><tr><th scope="col">ID</th><th scope="col">Nombre</th><th scope="col">Precio (€)</th><th scope="col">Acciones</th></tr></thead>
                <tbody>
                <c:forEach var="producto" items="${requestScope.productos}">
                    <tr>
                        <td><c:out value="${producto.id}" /></td>
                        <td><c:out value="${producto.nombre}" /></td>
                        <td><c:out value="${producto.precio}" /></td>
                        <td>
                            <form action="${pageContext.request.contextPath}/productos/editar" method="get">
                                <input type="hidden" name="id" value="${producto.id}">
                                <button type="submit">Editar</button>
                            </form>
                            <form action="${pageContext.request.contextPath}/productos/eliminar" method="post">
                                <input type="hidden" name="id" value="${producto.id}">
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
