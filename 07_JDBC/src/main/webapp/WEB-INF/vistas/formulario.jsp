<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="es">
<head><meta charset="UTF-8"><title>Producto</title></head>
<body>
<main>
    <h1><c:choose><c:when test="${requestScope.accion == 'editar'}">Editar producto</c:when><c:otherwise>Nuevo producto</c:otherwise></c:choose></h1>
    <c:if test="${not empty requestScope.error}"><p role="alert"><c:out value="${requestScope.error}" /></p></c:if>
    <form action="${pageContext.request.contextPath}/productos/${requestScope.accion}" method="post">
        <c:if test="${requestScope.accion == 'editar'}"><input type="hidden" name="id" value="${requestScope.producto.id}"></c:if>
        <p><label for="nombre">Nombre</label><br>
            <input id="nombre" name="nombre" maxlength="80" required value="<c:out value='${requestScope.producto.nombre}' />"></p>
        <p><label for="precio">Precio (€)</label><br>
            <input id="precio" name="precio" type="number" min="0" max="99999999.99" step="0.01" required
                   value="<c:out value='${requestScope.precioFormulario}' />"></p>
        <button type="submit">Guardar</button>
    </form>
    <p><a href="${pageContext.request.contextPath}/productos">Volver al listado</a></p>
</main>
</body>
</html>
